const CACHE_NAME = 'chatee-v1';
const ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon-192.png',
  './icon-512.png'
];

self.addEventListener('install', (e) => {
  e.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS);
    }).catch(err => console.log("Cache error during SW install", err))
  );
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            console.log('Deleting old cache:', key);
            return caches.delete(key);
          }
        })
      );
    }).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (e) => {
  // Solo cachear peticiones GET locales
  if (e.request.method === 'GET' && e.request.url.startsWith(self.location.origin)) {
    const url = new URL(e.request.url);
    
    // Para el punto de entrada y el index, priorizar siempre la red (Network-First)
    if (url.pathname === '/' || url.pathname === '/index.html') {
      e.respondWith(
        fetch(e.request)
          .then((response) => {
            return caches.open(CACHE_NAME).then((cache) => {
              cache.put(e.request, response.clone());
              return response;
            });
          })
          .catch(() => caches.match(e.request))
      );
    } else {
      // Estrategia Cache-First para recursos estaticos
      e.respondWith(
        caches.match(e.request).then((res) => {
          return res || fetch(e.request);
        })
      );
    }
  }
});

self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
