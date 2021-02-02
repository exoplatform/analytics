function() {
  return {
    init : function (settings, watchers) {
      if (settings && watchers && !this.watchers) {
        this.watchers = watchers;
        this.settings = settings;

        this.initCometd();
        this.installWatchers();
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
}();