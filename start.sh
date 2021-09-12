#!/bin/bash

npm install

echo "Starting in ${NODE_ENV} mode..."

if [ "${NODE_ENV}" = "production" ]; then
  npm run start
else
  npm run development
fi
