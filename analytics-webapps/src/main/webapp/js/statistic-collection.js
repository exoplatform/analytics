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
      const loc = window.location;
      cCometd.init({
        url: `${loc.protocol}//${loc.hostname}${(loc.port && ':') || ''}${loc.port || ''}/${this.settings.cometdContext}/cometd`,
        exoId: eXo.env.portal.userName,
        exoToken: this.settings.cometdToken,
        useWorkerScheduler: true,
      });
      this.cometdSubscription = cCometd.subscribe(this.settings.cometdChannel);
    },
    installWatchers : function() {
      const self_ = this;
      $(document).ready(() => {
        this.watchers.forEach(watcher => {
          if (!watcher || !watcher.domSelector) {
            return;
          }
          const $watcher = $(watcher.domSelector);
          // settings.maxItems is used to avoid attaching a lot of events
          if ($watcher.length) {
            $watcher.on(watcher.domEvent, (event) =>
              self_.sendMessage.call(self_, watcher, event)
            );
          }
        });
      });
    },
    sendMessage : function(watcher, event) {
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
  
        cCometd.publish(this.settings.cometdChannel, JSON.stringify({
          'name': watcher.name,
          'userName': eXo.env.portal.userName,
          'spaceId': eXo.env.portal.spaceId,
          'portalUri': eXo.env.server.portalBaseURL,
          'token': this.settings.cometdToken,
          'parameters' : parameters,
        }));
      } catch (e) {
        console.debug('Error sending data', e);
      }
    },
  };
}();