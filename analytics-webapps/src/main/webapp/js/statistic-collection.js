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
      this.cometdSubscription = cCometd.subscribe(this.settings.cometdChannel);
    },
    installWatchers : function() {
      const self_ = this;
      $(document).ready(() => {
        this.watchers.forEach(watcher => {
          if (!watcher || !watcher.selector) {
            return;
          }
          const $watcher = $(watcher.selector);
          // settings.maxItems is used to avoid attaching a lot of events
          if ($watcher.length) {
            $watcher.on(watcher.event, (event) =>
              self_.sendMessage.call(self_, watcher, event)
            );
          }
        });
      });
    },
    sendMessage : function(watcher, event) {
      const content = JSON.stringify({
        'event': 'addStatistic',
        'clientId': String(Date.now() + Number.parseInt(Math.random() * 10000)),
        'sender': eXo.env.portal.userName,
        'token': this.settings.cometdToken,
        'data' : {
          'name': watcher.name,
          'event': watcher.event,
          'sender': eXo.env.portal.userName,
          'currentPortalUrl': eXo.env.server.portalBaseURL,
          'currentSpaceId': eXo.env.portal.spaceId,
        }
      });

      cCometd.publish(this.settings.cometdChannel, content, (publishAck) => {
        console.debug('analytics sending : ', publishAck && publishAck.successful);
      });
    },
  };
}();