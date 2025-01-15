import xhook from "xhook";

type RequestBridgeT = {
  record: (type: string, json: string) => string;
}

declare global {
  interface Window {
    RequestBridge: RequestBridgeT,
    handlerRegistered: boolean
  }
}

if (!Boolean(window.handlerRegistered)) {
  xhook.after(function (request, response) {
    console.log(request.url);
    let type = null;
    if (request.url.match(/graphql$/)) {
      type = "feed";
    }
    if (request.url.match(/graphql\/query/)) {
      type = "reels";
    }
    if (request.url.match(/explore_grid/)) {
      type = "explore";
    }
    if (Boolean(type)) {
      const modifiedResponse = window.RequestBridge.record(type, response.text);
      if (modifiedResponse) {
        response.text = modifiedResponse;
      }
    }
  });
  window.handlerRegistered = true;
}
