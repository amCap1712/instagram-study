import HttpHeaders = browser.webRequest.HttpHeaders;

const BASE_URL = 'https://kiran-research2.comminfo.rutgers.edu/data-collector-admin';
const INJECT_ITEMS_URL = BASE_URL + '/inject/';
const injectItems: any[] = [];
let startRank = 0;
let feed_collection_id: string | null = null;
let reels_collection_id: string | null = null;
let explore_collection_id: string | null = null;

async function retrieveItems(): Promise<void> {
  const response = await fetch(INJECT_ITEMS_URL);
  const data = await response.json();
  injectItems.splice(0, injectItems.length);
  injectItems.push(...data.items);
}

interface RequestListenerArgs {
  requestId?: string;
  responseHeaders?: HttpHeaders;
  url?: string;
}

function removeCSP({ responseHeaders }: RequestListenerArgs): RequestListenerArgs {
  const headersToRemove = [
    'content-security-policy',
    'content-security-policy-report-only',
    'cross-origin-embedder-policy-report-only',
    'cross-origin-opener-policy',
    'cross-origin-resource-policy',
    'x-xss-protection',
    'permissions-policy',
    'document-policy'
  ];
  const newHeaders = responseHeaders
    ?.filter(header => !headersToRemove.includes(header.name.toLowerCase()));
  return { responseHeaders: newHeaders };
}

function interleaveEdges(edges: any[]): any[] {
  const itemsToInjectCount = Math.min(injectItems.length, edges.length);
  console.log("itemsToInjectCount", itemsToInjectCount);
  const newEdges = [];
  let index;
  for (index = 0; index < itemsToInjectCount; index++) {
    newEdges.push(injectItems.shift());
    newEdges.push(edges[index]);
  }
  for (; index < edges.length; index++) {
    newEdges.push(edges[index]);
  }
  return newEdges;
}

function injectItemsInResponse(response: any): string | undefined {
  if (response?.data?.xdt_api__v1__feed__timeline__connection) {
    console.log('inject items', JSON.stringify(injectItems));
    if (injectItems.length === 0) {
      return;
    }
    const edges = response.data.xdt_api__v1__feed__timeline__connection.edges;
    response.data.xdt_api__v1__feed__timeline__connection.edges = interleaveEdges(edges);
    const newResponse = JSON.stringify(response);
    port.postMessage("feed: " + newResponse);
    return newResponse;
  }
}

function interceptResponseListener({ requestId, url }: RequestListenerArgs): RequestListenerArgs {
  port.postMessage('url: ' + url);
  const filter = browser.webRequest.filterResponseData(requestId!);
  const buffers: ArrayBuffer[] = [];

  filter.ondata = (event) => {
    buffers.push(event.data);
  };

  filter.onstop = (event) => {
    const encoder = new TextEncoder();
    new Blob(buffers)
      .text()
      .then(text => {
        const body = JSON.parse(text);
        submitItems(body)
          .then(() => { console.log('submitted items'); })
          .catch(error => { console.log('error in submitting items', error); });
        console.log('url', url);
        console.log('response', body);
        const newBody = injectItemsInResponse(body);
        if (newBody) {
          filter.write(encoder.encode(newBody));
        } else {
          for (const buffer of buffers) {
            filter.write(buffer);
          }
        }
        filter.disconnect();
      });
  }

  return {};
}

async function submitNodes(source: string, nodes: any[]) {
  const promise = await browser.storage.local.get('survey_user_id');
  const survey_user_id = promise?.survey_user_id ?? "12345678";
  port.postMessage("survey user id: " + survey_user_id);

  const user_id = await browser.cookies.get({
    url: 'https://www.instagram.com/',
    name: 'ds_user_id',
  });
  const items: any[] = nodes.map((item: any, index: number) => ({
    ...item,
    rank: index + startRank
  }));
  startRank += items.length;

  const submitBody: any = {
    survey_user_id,
    user_id: user_id?.value,
    source,
    type: 'active',
    items
  };
  if (source === 'feed' && feed_collection_id) {
    submitBody.collection_id = feed_collection_id;
  } else if (source === 'reels' && reels_collection_id) {
    submitBody.collection_id = reels_collection_id;
  } else if (source === 'explore' && explore_collection_id) {
    submitBody.collection_id = explore_collection_id;
  }
  console.log("submission", JSON.stringify(submitBody));

  const submitResponse = await fetch(BASE_URL + '/' + source + '/', {
    body: JSON.stringify(submitBody),
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    }
  });
  const submitJson = await submitResponse.json();
  if (!submitResponse.ok) {
    throw new Error(JSON.stringify(submitJson));
  }
  if (source === 'feed') {
    feed_collection_id = submitBody.collection_id;
  } else if (source === 'reels') {
    reels_collection_id = submitBody.collection_id;
  } else if (source === 'explore') {
    explore_collection_id = submitBody.collection_id;
  }
}

async function submitItems(response: any): Promise<void> {
  let source: string | null = null;
  let nodes = [];
  if (response?.data?.xdt_api__v1__feed__timeline__connection) {
    source = 'feed';
    nodes = response.data.xdt_api__v1__feed__timeline__connection.edges.map((edge: any) => edge.node);
  } else if (response?.data?.xdt_api__v1__feed__home__connection) {
    source = 'feed';
    nodes = response.data.xdt_api__v1__feed__home__connection.edges.map((edge: any) => edge.node);
  } else if (response?.data?.xdt_api__v1__clips__home__connection) {
    source = 'reels';
    nodes = response.data.xdt_api__v1__clips__home__connection.edges.map((edge: any) => edge.node);
  } else if (response?.data?.xdt_api__v1__clips__home__connection_v2) {
    source = 'reels';
    nodes = response.data.xdt_api__v1__clips__home__connection_v2.edges.map((edge: any) => edge.node);
  } else if (response?.sectional_items) {
    port.postMessage('submission explore items');
    source = 'explore';
    nodes = response.sectional_items;
  }

  if (!source) {
    return;
  }

  await submitNodes(source, nodes);
}

function prepareInject({requestId}: RequestListenerArgs): void {
  const filter = browser.webRequest.filterResponseData(requestId!);
  const buffers: ArrayBuffer[] = [];

  console.log('starting retrieving inject items');
  const promise = retrieveItems()
    .then(() => { port.postMessage('retrieved inject items' + injectItems); })
    .catch((error) => {
      port.postMessage('error while retrieving inject items' + error.message);
    });
  filter.ondata = (event) => {
    buffers.push(event.data);
  };
  filter.onstop = async (event) => {
    promise
      .catch((e) => console.error(e))
      .finally(() => {
        const encoder = new TextEncoder();
        new Blob(buffers)
          .text()
          .then(text => injectPreload(text))
          .then((newText) => {
            filter.write(encoder.encode(newText));
            filter.disconnect();
          })
          .catch((e: Error) => {
            for (const buffer of buffers) {
              filter.write(buffer);
            }
            filter.disconnect();
            console.error("error during preload inject", e);
            port.postMessage("error during preload inject " + e.message)
          });
    })
  };
}

browser.webRequest.onHeadersReceived.addListener(
  removeCSP,
  { urls: ['<all_urls>'] },
  ['blocking', 'responseHeaders'],
);

browser.webRequest.onBeforeRequest.addListener(
  prepareInject,
  { urls: ['https://www.instagram.com/'] },
  ['blocking'],
)

browser.webRequest.onBeforeRequest.addListener(
  interceptResponseListener,
  {
    urls: [
      'https://www.instagram.com/graphql',
      'https://www.instagram.com/graphql?*',
      'https://www.instagram.com/graphql/query*',
      'https://www.instagram.com/graphql/query?*',
      'https://www.instagram.com/api/graphql',
      'https://www.instagram.com/api/graphql?*',
      'https://www.instagram.com/api/v1/discover/web/explore_grid*',
      'https://www.instagram.com/api/v1/discover/web/explore_grid?*',
    ]
  },
  ['blocking'],
);

const port = browser.runtime.connectNative("browser");
port.onMessage.addListener((response: any) => {
  if (response?.survey_user_id) {
    browser.storage.local.set({survey_user_id: response.survey_user_id}).then(r => console.log(r));
  }
  port.postMessage(`Received: ${JSON.stringify(response)}`);
});
port.postMessage("Hello from WebExtension!");


async function injectPreload(html: string): Promise<any> {
  port.postMessage("Original HTML: " + html);
  const parser = new DOMParser();
  const preloadDocument = parser.parseFromString(html, "text/html");
  const scripts = preloadDocument.querySelectorAll("script[data-sjs]");

  for (const script of Array.from(scripts)) {
    const data = script.innerHTML;
    const json = JSON.parse(data);
    const result = json?.require?.[0]?.[3]?.[0]?.__bbox?.require?.[0]?.[3]?.[1]?.__bbox?.result?.data?.xdt_api__v1__feed__timeline__connection;
    if (result) {
      console.log("script found", script.innerHTML);
      const edges = result.edges;

      const nodes = edges.map((edge: any) => edge.node);
      try {
        await submitNodes("feed", nodes);
        console.log('submitted items');
      } catch (error) {
        console.log('error in submitting items', error);
      }

      result.edges = interleaveEdges(edges);
      console.log("preload injected", result);

      const newJson = JSON.stringify(json);
      const scriptHtml = script as HTMLScriptElement;
      scriptHtml.dataset.contentLen = newJson.length.toString();
      script.innerHTML = newJson;

      const newHtml = "<!DOCTYPE html>" + preloadDocument.documentElement.outerHTML;
      port.postMessage("Modified HTML: " + html);
      return newHtml;
    }
  }

  return html;
}
