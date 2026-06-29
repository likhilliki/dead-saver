# --- Stage 1: Build Frontend Assets ---
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# --- Stage 2: Build Backend Server ---
FROM node:20-alpine AS backend-builder
WORKDIR /app/backend
COPY backend/package*.json ./
RUN npm install
COPY backend/ ./
RUN npm run build

# --- Stage 3: Production Runner ---
FROM node:20-alpine
WORKDIR /app

# Copy built backend files and dependencies
COPY --from=backend-builder /app/backend/dist ./backend/dist
COPY --from=backend-builder /app/backend/node_modules ./backend/node_modules
COPY --from=backend-builder /app/backend/package.json ./backend/package.json

# Copy built frontend assets to where the Express server will serve them
COPY --from=frontend-builder /app/frontend/dist ./frontend/dist

# Expose Port 8080 as requested
EXPOSE 8080

# Environment setups
ENV PORT=8080
ENV NODE_ENV=production

# Start the built server
WORKDIR /app/backend
CMD ["node", "dist/server.js"]
