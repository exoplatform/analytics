function() {
  const api = {
    init : function (settings, watchers) {
      if (settings && watchers && !this.watchers) {
        this.watchers = watchers;
        this.settings = settings;

        this.initCometd();
        this.installWatchers();
        const _self = this;
        document.addEventListener("analytics-install-watchers", function() {
          _self.installWatchers();
        });
      }
    },
    initCometd : function() {
      const self_ = this;
      cCometd.addListener('/meta/connect', function (message) {
        self_.connected = !cCometd.isDisconnected();
      });
      cCometd.addListener('/meta/disconnect', function(message) {
        self_.connected = cCometd.isDisconnected();
      });
      cCometd.addListener('/meta/handshake', function (handshake) {
        self_.connected = handshake && handshake.successful;
      });
      const loc = window.location;
      cCometd.init({
        url: `${loc.protocol}//${loc.hostname}${(loc.port && ':') || ''}${loc.port || ''}/${this.settings.cometdContext}/cometd`,
        exoId: eXo.env.portal.userName,
        exoToken: this.settings.cometdToken,
        useWorkerScheduler: true,
      });

      if (!this.cometdSubscription) {
        // First time init, install listener
        document.addEventListener('exo-statistic-message', event => this.sendMessage(event && event.detail));
      }

      this.cometdSubscription = cCometd.subscribe(this.settings.cometdChannel, null, event => {}, null, (subscribeReply) => {
        self_.connected = subscribeReply && subscribeReply.successful;
      });
    },
    installWatchers : function() {
      const self_ = this;
      $(document).ready(() => {
        window.setTimeout(() => {
          self_.watchers.forEach(watcher => {
            self_.installWatcher.call(self_, watcher, 3);
          });
        }, 100);
      });
    },
    installWatcher : function(watcher, retries) {
      if (retries-- <= 0 || !watcher || !watcher.domSelector) {
        return;
      }
      const $watcher = $(watcher.domSelector);
      $watcher.data('watched', 'true');
      // settings.maxItems is used to avoid attaching a lot of events
      const self_ = this;
      if ($watcher.length) {
        $watcher.off(watcher.domEvent).on(watcher.domEvent, (event) => {
          self_.sendWatcherMessage.call(self_, watcher, event, self_.connected);
        });
      } else {
        window.setTimeout(() => {
          self_.installWatcher(watcher, retries);
        }, 1000);
      }
    },
    sendWatcherMessage : function(watcher, event) {
      try {
        const parameters = {};
        if (watcher.domProperties) {
          watcher.domProperties.forEach(property => {
            if ($(event.currentTarget).attr(property)) {
              parameters[`dom-${property}`] = $(event.currentTarget).attr(property);
            } else if ($(event.currentTarget).data(property)) {
              parameters[`dom-data-${property}`] = $(event.currentTarget).data(property);
            }
          });
        }
  
        if (watcher.domEventProperties) {
          watcher.domEventProperties.forEach(property => {
            if (event[property]) {
              parameters[`event-${property}`] = event[property];
            }
          });
        }

        const statisticMessage = {
          'name': watcher.name,
          'userName': eXo.env.portal.userName,
          'spaceId': eXo.env.portal.spaceId,
          'portalUri': eXo.env.server.portalBaseURL,
          'parameters' : parameters,
        };
        this.sendMessage(statisticMessage);
      } catch (e) {
        console.debug('Error sending data', e);
      }
    },
    sendMessage : function(statisticMessage) {
      if (statisticMessage) {
        statisticMessage.token = this.settings.cometdToken;
        cCometd.publish(this.settings.cometdChannel, JSON.stringify(statisticMessage));
      }
    },
  };

  eXo.env.portal.onLoadCallbacks.push(() => {
    const nowDate = Date.now();
    if (eXo.env.portal.requestStartTime && nowDate > eXo.env.portal.requestStartTime) {
      const isMobile = navigator.userAgentData.mobile;
      const loadingTime = nowDate - eXo.env.portal.requestStartTime;
      const loadingTimeStyle = (loadingTime > (isMobile && 5000 || 3000)) && 'color:red;font-weight:bold;' || 'color:green;font-weight:bold;';
      if (eXo.developing) {
        console.warn(`%cPage displayed within: %c${loadingTime} %cms`,
          'font-weight:bold;',
          loadingTimeStyle,
          '');
      }
      window.setTimeout(() => {
        api.sendMessage({
          name: 'pageUIDisplay',
          operation: 'pageUIDisplay',
          userName: eXo.env.portal.userName,
          spaceId: eXo.env.portal.spaceId,
          parameters: {
            duration: loadingTime,
            portalName: eXo.env.portal.portalName,
            portalUri: eXo.env.server.portalBaseURL,
            pageUri: window.location.pathname,
            pageTitle: eXo.env.portal.pageTitle,
            pageUri: eXo.env.portal.selectedNodeUri,
            applicationNames: eXo.env.portal.applicationNames,
            isMobile,
          },
        });
      }, 500);
    }
  });

  require(['SHARED/vue'], () => {
    if (eXo.env.portal.requestStartTime) {
      eXo.env.portal.loadingAppsStartTime = {};
      document.addEventListener('vue-app-loading-start', event => {
        const appName = event && event.detail;
        if (eXo.env.portal.requestStartTime && appName && !eXo.env.portal.loadingAppsStartTime[appName]) {
          const now = Date.now();
          eXo.env.portal.loadingAppsStartTime[appName] = {
            start: now,
          };
          const startLoadingTime = now - eXo.env.portal.requestStartTime;
          const startTimeStyle = startLoadingTime > 3000 && 'color:red;font-weight:bold;' || 'color:green;font-weight:bold;';
          if (eXo.developing) {
            // eslint-disable-next-line no-console
            console.debug(`App %c${appName}%c Start Loading at: %c${startLoadingTime} %cms`,
              'font-weight:bold;',
              '',
              startTimeStyle,
              '');
          }
          api.sendMessage();
        }
      });
      document.addEventListener('vue-app-loading-end', event => {
        const appName = event && event.detail;
        if (!appName) {
          // eslint-disable-next-line no-console
          console.warn('Missing Application name, please verify that "data" attribute near Vue.create is of type object');
        } else if (eXo.env.portal.requestStartTime && eXo.env.portal.loadingAppsStartTime[appName]) {
          const start = eXo.env.portal.loadingAppsStartTime[appName].start;
          delete eXo.env.portal.loadingAppsStartTime[appName];
          const end = Date.now();
          const startLoadingTime = start - eXo.env.portal.requestStartTime;
          const endLoadingTime = end - eXo.env.portal.requestStartTime;
          const durationLoadingTime = end - start;
          const startTimeStyle = startLoadingTime > 3000 && 'color:red;font-weight:bold;' || 'color:green;font-weight:bold;';
          const endTimeStyle = endLoadingTime > 3000 && 'color:red;font-weight:bold;' || 'color:green;font-weight:bold;';
          const durationTimeStyle = durationLoadingTime > 1000 && 'color:red;font-weight:bold;' || 'color:green;font-weight:bold;';
          if (eXo.developing) {
            // eslint-disable-next-line no-console
            console.debug(`App %c${appName}%c
               Started at: %c${startLoadingTime} %cms
               End at: %c${endLoadingTime} %cms
               Duration : %c${durationLoadingTime} %cms`, 
            'font-weight:bold;',
            '',
            startTimeStyle,
            '',
            endTimeStyle,
            '',
            durationTimeStyle,
            '');
          }

          window.setTimeout(() => {
            api.sendMessage({
              name: 'pageUIDisplay',
              operation: 'applicationUIDisplay',
              userName: eXo.env.portal.userName,
              spaceId: eXo.env.portal.spaceId,
              parameters: {
                duration: durationLoadingTime,
                portalName: eXo.env.portal.portalName,
                portalUri: eXo.env.server.portalBaseURL,
                pageUri: window.location.pathname,
                pageTitle: eXo.env.portal.pageTitle,
                pageUri: eXo.env.portal.selectedNodeUri,
                applicationName: appName,
                startLoadingTime: startLoadingTime,
                endLoadingTime: endLoadingTime,
              },
            });
          }, 500);

          if (!Object.keys(eXo.env.portal.loadingAppsStartTime).length) {
            window.setTimeout(() => {
              if (!eXo.env.portal.loadingAppsFinished && !Object.keys(eXo.env.portal.loadingAppsStartTime).length) {
                eXo.env.portal.loadingAppsFinished = true;
                if (eXo.developing) {
                  // eslint-disable-next-line no-console
                  console.warn(`Overall %cpage applications%c finished loading at : %c${endLoadingTime} %cms`, 
                    'font-weight:bold;',
                    '',
                    endTimeStyle,
                    '');
                }
                api.sendMessage({
                  name: 'pageUIDisplay',
                  operation: 'pageFullUIDisplay',
                  userName: eXo.env.portal.userName,
                  spaceId: eXo.env.portal.spaceId,
                  parameters: {
                    duration: endLoadingTime,
                    portalName: eXo.env.portal.portalName,
                    portalUri: eXo.env.server.portalBaseURL,
                    pageUri: window.location.pathname,
                    pageTitle: eXo.env.portal.pageTitle,
                    pageUri: eXo.env.portal.selectedNodeUri,
                    applicationNames: eXo.env.portal.applicationNames,
                    isMobile: navigator.userAgentData.mobile,
                  },
                });
              }
            }, 1000);
          }
        }
      });
    }
  });

  return api;
}();