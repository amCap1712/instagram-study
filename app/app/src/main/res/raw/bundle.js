(()=>{"use strict";var e={};e.g=function(){if("object"==typeof globalThis)return globalThis;try{return this||new Function("return this")()}catch(e){if("object"==typeof window)return window}}();const t=(e,t)=>Array.prototype.slice.call(e,t);let n=null;"undefined"!=typeof WorkerGlobalScope&&self instanceof WorkerGlobalScope?n=self:void 0!==e.g?n=e.g:window&&(n=window);const r=n,o=n.document,s=["load","loadend","loadstart"],a=["progress","abort","error","timeout"],i=e=>["returnValue","totalSize","position"].includes(e),c=function(e,t){for(let n in e){if(i(n))continue;const r=e[n];try{t[n]=r}catch(e){}}return t},u=function(e,t,n){const r=e=>function(r){const o={};for(let e in r){if(i(e))continue;const s=r[e];o[e]=s===t?n:s}return n.dispatchEvent(e,o)};for(let o of Array.from(e))n._has(o)&&(t[`on${o}`]=r(o))},l=function(e){let n={};const r=e=>n[e]||[],s={addEventListener:function(e,t,o){n[e]=r(e),n[e].indexOf(t)>=0||(o=void 0===o?n[e].length:o,n[e].splice(o,0,t))},removeEventListener:function(e,t){if(void 0===e)return void(n={});void 0===t&&(n[e]=[]);const o=r(e).indexOf(t);-1!==o&&r(e).splice(o,1)},dispatchEvent:function(){const n=t(arguments),a=n.shift();e||(n[0]=c(n[0],function(e){if(o&&null!=o.createEventObject){const t=o.createEventObject();return t.type=e,t}try{return new Event(e)}catch(t){return{type:e}}}(a)));const i=s[`on${a}`];i&&i.apply(s,n);const u=r(a).concat(r("*"));for(let e=0;e<u.length;e++)u[e].apply(s,n)},_has:e=>!(!n[e]&&!s[`on${e}`])};return e&&(s.listeners=e=>t(r(e)),s.on=s.addEventListener,s.off=s.removeEventListener,s.fire=s.dispatchEvent,s.once=function(e,t){var n=function(){return s.off(e,n),t.apply(null,arguments)};return s.on(e,n)},s.destroy=()=>n={}),s};var d=function(e,t){switch(typeof e){case"object":return n=e,Object.entries(n).map((([e,t])=>`${e.toLowerCase()}: ${t}`)).join("\r\n");case"string":return function(e,t){const n=e.split("\r\n");null==t&&(t={});for(let e of n)if(/([^:]+):\s*(.+)/.test(e)){const e=null!=RegExp.$1?RegExp.$1.toLowerCase():void 0,n=RegExp.$2;null==t[e]&&(t[e]=n)}return t}(e,t)}var n;return[]};const f=l(!0),p=e=>void 0===e?null:e,h=r.XMLHttpRequest,y=function(){const e=new h,t={};let n,r,o,i=null;var y=0;const v=function(){if(o.status=i||e.status,-1!==i&&(o.statusText=e.statusText),-1===i);else{const t=d(e.getAllResponseHeaders());for(let e in t){const n=t[e];if(!o.headers[e]){const t=e.toLowerCase();o.headers[t]=n}}}},g=function(){w.status=o.status,w.statusText=o.statusText},E=function(){n||w.dispatchEvent("load",{}),w.dispatchEvent("loadend",{}),n&&(w.readyState=0)},b=function(e){for(;e>y&&y<4;)w.readyState=++y,1===y&&w.dispatchEvent("loadstart",{}),2===y&&g(),4===y&&(g(),"text"in o&&(w.responseText=o.text),"xml"in o&&(w.responseXML=o.xml),"data"in o&&(w.response=o.data),"finalUrl"in o&&(w.responseURL=o.finalUrl)),w.dispatchEvent("readystatechange",{}),4===y&&(!1===t.async?E():setTimeout(E,0))},m=function(e){if(4!==e)return void b(e);const n=f.listeners("after");var r=function(){if(n.length>0){const e=n.shift();2===e.length?(e(t,o),r()):3===e.length&&t.async?e(t,o,r):r()}else b(4)};r()};var w=l();t.xhr=w,e.onreadystatechange=function(t){try{2===e.readyState&&v()}catch(e){}4===e.readyState&&(r=!1,v(),function(){if(e.responseType&&"text"!==e.responseType)"document"===e.responseType?(o.xml=e.responseXML,o.data=e.responseXML):o.data=e.response;else{o.text=e.responseText,o.data=e.responseText;try{o.xml=e.responseXML}catch(e){}}"responseURL"in e&&(o.finalUrl=e.responseURL)}()),m(e.readyState)};const x=function(){n=!0};w.addEventListener("error",x),w.addEventListener("timeout",x),w.addEventListener("abort",x),w.addEventListener("progress",(function(t){y<3?m(3):e.readyState<=3&&w.dispatchEvent("readystatechange",{})})),"withCredentials"in e&&(w.withCredentials=!1),w.status=0;for(let e of Array.from(a.concat(s)))w[`on${e}`]=null;if(w.open=function(e,s,a,i,c){y=0,n=!1,r=!1,t.headers={},t.headerNames={},t.status=0,t.method=e,t.url=s,t.async=!1!==a,t.user=i,t.pass=c,o={},o.headers={},m(1)},w.send=function(n){let i,l;for(i of["type","timeout","withCredentials"])l="type"===i?"responseType":i,l in w&&(t[i]=w[l]);t.body=n;const d=f.listeners("before");var p=function(){if(!d.length)return function(){for(i of(u(a,e,w),w.upload&&u(a.concat(s),e.upload,w.upload),r=!0,e.open(t.method,t.url,t.async,t.user,t.pass),["type","timeout","withCredentials"]))l="type"===i?"responseType":i,i in t&&(e[l]=t[i]);for(let n in t.headers){const r=t.headers[n];n&&e.setRequestHeader(n,r)}e.send(t.body)}();const n=function(e){if("object"==typeof e&&("number"==typeof e.status||"number"==typeof o.status))return c(e,o),"data"in e||(e.data=e.response||e.text),void m(4);p()};n.head=function(e){c(e,o),m(2)},n.progress=function(e){c(e,o),m(3)};const f=d.shift();1===f.length?n(f(t)):2===f.length&&t.async?f(t,n):n()};p()},w.abort=function(){i=-1,r?e.abort():w.dispatchEvent("abort",{})},w.setRequestHeader=function(e,n){const r=null!=e?e.toLowerCase():void 0,o=t.headerNames[r]=t.headerNames[r]||e;t.headers[o]&&(n=t.headers[o]+", "+n),t.headers[o]=n},w.getResponseHeader=e=>p(o.headers[e?e.toLowerCase():void 0]),w.getAllResponseHeaders=()=>p(d(o.headers)),e.overrideMimeType&&(w.overrideMimeType=function(){e.overrideMimeType.apply(e,arguments)}),e.upload){let e=l();w.upload=e,t.upload=e}return w.UNSENT=0,w.OPENED=1,w.HEADERS_RECEIVED=2,w.LOADING=3,w.DONE=4,w.response="",w.responseText="",w.responseXML=null,w.readyState=0,w.statusText="",w};y.UNSENT=0,y.OPENED=1,y.HEADERS_RECEIVED=2,y.LOADING=3,y.DONE=4;var v={patch(){h&&(r.XMLHttpRequest=y)},unpatch(){h&&(r.XMLHttpRequest=h)},Native:h,Xhook:y};const g=r.fetch;function E(e){return e instanceof Headers?b([...e.entries()]):Array.isArray(e)?b(e):e}function b(e){return e.reduce(((e,[t,n])=>(e[t]=n,e)),{})}const m=function(e,t={headers:{}}){let n=Object.assign(Object.assign({},t),{isFetch:!0});if(e instanceof Request){const r=function(e){let t={};return["method","headers","body","mode","credentials","cache","redirect","referrer","referrerPolicy","integrity","keepalive","signal","url"].forEach((n=>t[n]=e[n])),t}(e),o=Object.assign(Object.assign({},E(r.headers)),E(n.headers));n=Object.assign(Object.assign(Object.assign({},r),t),{headers:o,acceptedRequest:!0})}else n.url=e;const r=f.listeners("before"),o=f.listeners("after");return new Promise((function(e,t){let s=e;const a=function(e){if(!o.length)return s(e);const t=o.shift();return 2===t.length?(t(n,e),a(e)):3===t.length?t(n,e,a):a(e)},i=function(t){if(void 0!==t){const n=new Response(t.body||t.text,t);return e(n),void a(n)}c()},c=function(){if(!r.length)return void u();const e=r.shift();return 1===e.length?i(e(n)):2===e.length?e(n,i):void 0},u=()=>{const{url:e,isFetch:r,acceptedRequest:o}=n,i=function(e,t){var n={};for(var r in e)Object.prototype.hasOwnProperty.call(e,r)&&t.indexOf(r)<0&&(n[r]=e[r]);if(null!=e&&"function"==typeof Object.getOwnPropertySymbols){var o=0;for(r=Object.getOwnPropertySymbols(e);o<r.length;o++)t.indexOf(r[o])<0&&Object.prototype.propertyIsEnumerable.call(e,r[o])&&(n[r[o]]=e[r[o]])}return n}(n,["url","isFetch","acceptedRequest"]);g(e,i).then((e=>a(e))).catch((function(e){return s=t,a(e),t(e)}))};c()}))};var w={patch(){g&&(r.fetch=m)},unpatch(){g&&(r.fetch=g)},Native:g,Xhook:m};const x=f;x.EventEmitter=l,x.before=function(e,t){if(e.length<1||e.length>2)throw"invalid hook";return x.on("before",e,t)},x.after=function(e,t){if(e.length<2||e.length>3)throw"invalid hook";return x.on("after",e,t)},x.enable=function(){v.patch(),w.patch()},x.disable=function(){v.unpatch(),w.unpatch()},x.XMLHttpRequest=v.Native,x.fetch=w.Native,x.headers=d,x.enable(),Boolean(window.handlerRegistered)||(x.after((function(e,t){console.log(e.url);var n=null;if(e.url.match(/graphql$/)&&(n="feed"),e.url.match(/graphql\/query/)&&(n="reels"),e.url.match(/explore_grid/)&&(n="explore"),Boolean(n)){var r=window.RequestBridge.record(n,t.text);r&&(t.text=r)}})),window.handlerRegistered=!0)})();