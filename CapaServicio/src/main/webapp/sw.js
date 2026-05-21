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

self.addEventListener('fetch', (e) => {
  // Solo cachear peticiones GET locales
  if (e.request.method === 'GET' && e.request.url.startsWith(self.location.origin)) {
    e.respondWith(
      caches.match(e.request).then((res) => {
        return res || fetch(e.request);
      })
    );
  }
});
