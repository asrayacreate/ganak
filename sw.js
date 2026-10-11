var C="ganak-v9-3";
self.addEventListener("install",function(e){
  e.waitUntil(caches.open(C).then(function(c){
    /* cache:"reload" skips the browser's HTTP cache, so a new version never precaches a stale page */
    return c.addAll(["./","./index.html","./manifest.json","./icon-192.png","./icon-512.png"].map(function(u){ return new Request(u,{cache:"reload"}); }));
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
  var req=e.request;
  if(req.method!=="GET") return;
  var url=new URL(req.url);
  if(url.origin===self.location.origin){
    /* stale-while-revalidate: open instantly from cache (offline works), refresh the copy in the
       background so the next launch has the newest index.html even if C was not bumped */
    var net=fetch(req).then(function(res){
      if(!res || !res.ok) return res;
      var cp=res.clone();
      return caches.open(C).then(function(c){ return c.put(req,cp); }).then(function(){ return res; },function(){ return res; });
    });
    e.waitUntil(net.catch(function(){}));
    e.respondWith(caches.match(req,{ignoreSearch:true}).then(function(hit){
      return hit || net.catch(function(){ return req.mode==="navigate" ? caches.match("./index.html") : Response.error(); });
    }));
    return;
  }
  if(/^fonts\.(googleapis|gstatic)\.com$/.test(url.hostname)){
    /* logo font: cache-first so the header still looks right offline; analytics etc. go straight to network */
    e.respondWith(caches.match(req).then(function(hit){
      return hit || fetch(req).then(function(res){
        var cp=res.clone(); caches.open(C).then(function(c){ c.put(req,cp); });
        return res;
      });
    }));
  }
});
