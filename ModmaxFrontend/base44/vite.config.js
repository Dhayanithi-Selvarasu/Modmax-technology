import { fileURLToPath } from "node:url";
import { readFileSync } from "node:fs";
import { cloudflare } from "@cloudflare/vite-plugin";
import { tanstackStart } from "@tanstack/react-start/plugin/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

const NOT_PREBUNDLED = new Set([
  "react",
  "react-dom",
  "@tanstack/react-start",
  "@tanstack/react-router",
  "@tanstack/react-router-ssr-query",
  "tailwindcss-animate",
]);

const { dependencies } = JSON.parse(
  readFileSync(
    new URL("./package.json", import.meta.url),
    "utf8"
  )
);

const PREBUNDLE = Object.keys(dependencies).filter(
  (name) => !NOT_PREBUNDLED.has(name)
);

const BROWSER_PREBUNDLE = [
  "@tanstack/react-router",
  "@tanstack/react-router/ssr/client",
  "@tanstack/router-core",
  "@tanstack/router-core/isServer",
  "@tanstack/router-core/ssr/client",
  "@tanstack/router-core/scroll-restoration-script",
  "@tanstack/history",
  "@tanstack/start-fn-stubs",
  "seroval",
  "seroval-plugins/web",
  "cookie-es",
];

export default defineConfig({
  optimizeDeps: {
    include: [...PREBUNDLE, ...BROWSER_PREBUNDLE],
  },

  environments: {
    ssr: {
      optimizeDeps: {
        include: PREBUNDLE,
      },
      build: {
        sourcemap: true,
        rolldownOptions: {
          output: {
            sourcemapExcludeSources: true,
          },
        },
      },
    },
  },
  resolve: {
  alias: {
    "@": fileURLToPath(new URL("./src", import.meta.url))
  }
},

  plugins: [
    cloudflare({
      viteEnvironment: { name: "ssr" },
      inspectorPort: false,
    }),

    tanstackStart({
      srcDirectory: "src",
    }),

    react(),
  ],
});
