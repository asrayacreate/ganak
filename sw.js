var C="ganak-v6-2";
self.addEventListener("install",function(e){
  e.waitUntil(caches.open(C).then(function(c){
    return c.addAll(["./","./index.html","./manifest.json","./icon-192.png","./icon-512.png"]);
  }));
  self.skipWaiting();
});
self.addEventListener("activate",function(e){
  e.waitUntil(caches.keys().then(function(ks){
    return Promise.all(ks.filter(function(k){return k!==C;}).map(function(k){return caches.delete(k);}));
  }));
  self.clients.claim();
});
self.addEventListener("fetch",function(e){
  if(e.request.method!=="GET") return;
  e.respondWith(caches.match(e.request,{ignoreSearch:true}).then(function(r){
    return r || fetch(e.request).then(function(res){
      var cp=res.clone();
      caches.open(C).then(function(c){ c.put(e.request,cp); });
      return res;
    }).catch(function(){ return caches.match("./index.html"); });
  }));
});
