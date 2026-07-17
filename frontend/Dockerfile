# syntax=docker/dockerfile:1

# --- Build stage: produce the static SPA bundle ---
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

# Build-time API base URL (baked into the bundle). Empty => the SPA uses "/api",
# which nginx proxies to the backend. Override for a cross-origin API deployment.
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL

COPY . .
RUN npm run build

# --- Runtime stage: serve the bundle with nginx (also proxies /api to the backend) ---
FROM nginx:1.27-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
