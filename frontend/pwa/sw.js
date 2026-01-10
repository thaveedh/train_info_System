self.addEventListener("install", (event) => {
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  // nothing for now
});

self.addEventListener("fetch", () => {
  // default network behavior
});
