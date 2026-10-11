var C="ganak-v9-5", D="ganak-days";   /* D: the "आजको मिति" texts written by the page — kept across versions */
self.addEventListener("install",function(e){
  e.waitUntil(caches.open(C).then(function(c){
    /* cache:"reload" skips the browser's HTTP cache, so a new version never precaches a stale page */
    return c.addAll(["./","./index.html","./manifest.json","./icon-192.png","./icon-512.png"].map(function(u){ return new Request(u,{cache:"reload"}); }));
  }));
  self.skipWaiting();
});
self.addEventListener("activate",function(e){
  e.waitUntil(caches.keys().then(function(ks){
    return Promise.all(ks.filter(function(k){return k!==C&&k!==D;}).map(function(k){return caches.delete(k);}));
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
/* 📲 today's date on the phone screen: once a day (or when the app asks) show today's text quietly */
function todayNote(force){
  return caches.open(D).then(function(c){
    return c.match("./__days.json").then(function(r){ return r ? r.json() : null; }).then(function(j){
      if(!j || !j.on) return;
      var day = new Date(Date.now() + 345*60000).toISOString().slice(0,10), x = j.days && j.days[day];   /* Nepal date (UTC+5:45) */
      if(!x || (!force && j.shown === day)) return;
      return self.registration.showNotification(x.t, {body:x.b, tag:"ganak-today", renotify:false, silent:true,
        icon:x.ic || "./icon-192.png", badge:x.bd || "./icon-192.png", data:{u:"./#patro"}}).then(function(){
        j.shown = day; return c.put("./__days.json", new Response(JSON.stringify(j), {headers:{"Content-Type":"application/json"}}));
      });
    });
  }).catch(function(){});
}
self.addEventListener("message",function(e){ if(e.data && e.data.k==="ganak-today") e.waitUntil(todayNote(e.data.force)); });
self.addEventListener("periodicsync",function(e){ if(e.tag==="ganak-today") e.waitUntil(todayNote(false)); });
self.addEventListener("notificationclick",function(e){
  e.notification.close();
  e.waitUntil(self.clients.matchAll({type:"window",includeUncontrolled:true}).then(function(ws){
    for(var i=0;i<ws.length;i++){ if("focus" in ws[i]) return ws[i].focus(); }
    return self.clients.openWindow((e.notification.data && e.notification.data.u) || "./");
  }));
});
