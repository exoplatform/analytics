export function loadUser(users, userId) {
  if (!userId) {
    return Promise.resolve(null);
  }
  if (users[userId]) {
    return Promise.resolve(users[userId]);
  } else {
    return fetch(`/portal/rest/v1/social/identities/${userId}`)
    .then((resp) => {
      if (resp && resp.ok) {
        return resp.json();
      }
    })
    .then((obj) => {
      obj = obj || {};
      const userObject = {
        identityId: userId,
        providerId: obj.globalId && obj.globalId.domain,
        remoteId: obj.globalId && obj.globalId.localId,
        avatar: obj.avatar || '/eXoSkin/skin/images/system/UserAvtDefault.png',
        displayName: obj.profile && obj.profile.fullname,
      };
      users[userId] = userObject;
    })
  }
}

export function getPages(siteType, siteName) {
  let url;
  if(siteType === eXo.env.portal.containerName) {
    url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/${siteType}/?siteName=${siteName}&scope=all&visibility=displayed&visibility=system`;
  } else {
    url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/${siteType}/?scope=all&visibility=displayed&visibility=system`
  }
  return fetch(url, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if(!resp || !resp.ok) {
      throw new Error('Error retrieving pages of current site');
    }
    return resp.json();
  }).then((allPages) => {
    let analyticsPages ;
    if (siteType === eXo.env.portal.containerName) {
      analyticsPages = allPages.filter(site => site.name === 'analytics');
    } else {
      const pages = allPages.filter(site => site.name === eXo.env.portal.spaceName)[0].children;
      if (pages) {
        analyticsPages = pages.filter(site => site.name === 'analytics');
      }
    }
    return analyticsPages;
  })
}

export function loadSpace(spaces, spaceId) {
  if (!spaceId) {
    return Promise.resolve(null);
  }
  if (spaces[spaceId]) {
    return Promise.resolve(spaces[spaceId]);
  } else {
    let spaceDisplayName;
    return fetch(`/portal/rest/v1/social/spaces/${spaceId}`)
    .then((resp) => {
      if (resp && resp.ok) {
        return resp.json();
      }
    })
    .then(obj => {
      if (obj && obj.identity) {
        spaceDisplayName = obj.displayName;
        return fetch(`${obj.identity}`)
          .then((resp) => resp && resp.ok && resp.json());
      }
    })
    .then((obj) => {
      obj = obj || {};
      const spaceObject = {
        identityId: obj.id,
        providerId: obj.globalId && obj.globalId.domain,
        remoteId: obj.globalId && obj.globalId.localId,
        avatar: obj.avatar || '/eXoSkin/skin/images/system/SpaceAvtDefault.png',
          spaceId: spaceId,
          displayName: spaceDisplayName,
        };
        spaces[spaceId] = spaceObject;
      })
    }
}
