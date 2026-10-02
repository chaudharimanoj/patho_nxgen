FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
ENV NODE_OPTIONS="--experimental-require-module"
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/patho-nxgen/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
