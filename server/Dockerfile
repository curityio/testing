FROM node:9

RUN mkdir /src

WORKDIR /src
ADD package.json /src/package.json
ADD server.js /src/server.js

RUN npm install

EXPOSE 8100

CMD npm start