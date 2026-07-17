/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base URL the SPA uses for API calls. Empty/unset falls back to "/api" (proxied). */
  readonly VITE_API_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
