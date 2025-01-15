/******/ (() => { // webpackBootstrap
var __webpack_exports__ = {};
function _typeof(obj) { "@babel/helpers - typeof"; return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function (obj) { return typeof obj; } : function (obj) { return obj && "function" == typeof Symbol && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }, _typeof(obj); }
function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ownKeys(Object(source), !0).forEach(function (key) { _defineProperty(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }
function _defineProperty(obj, key, value) { key = _toPropertyKey(key); if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }
function _toPropertyKey(arg) { var key = _toPrimitive(arg, "string"); return _typeof(key) === "symbol" ? key : String(key); }
function _toPrimitive(input, hint) { if (_typeof(input) !== "object" || input === null) return input; var prim = input[Symbol.toPrimitive]; if (prim !== undefined) { var res = prim.call(input, hint || "default"); if (_typeof(res) !== "object") return res; throw new TypeError("@@toPrimitive must return a primitive value."); } return (hint === "string" ? String : Number)(input); }
function _toConsumableArray(arr) { return _arrayWithoutHoles(arr) || _iterableToArray(arr) || _unsupportedIterableToArray(arr) || _nonIterableSpread(); }
function _nonIterableSpread() { throw new TypeError("Invalid attempt to spread non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); }
function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }
function _iterableToArray(iter) { if (typeof Symbol !== "undefined" && iter[Symbol.iterator] != null || iter["@@iterator"] != null) return Array.from(iter); }
function _arrayWithoutHoles(arr) { if (Array.isArray(arr)) return _arrayLikeToArray(arr); }
function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) arr2[i] = arr[i]; return arr2; }
function _regeneratorRuntime() { "use strict"; /*! regenerator-runtime -- Copyright (c) 2014-present, Facebook, Inc. -- license (MIT): https://github.com/facebook/regenerator/blob/main/LICENSE */ _regeneratorRuntime = function _regeneratorRuntime() { return exports; }; var exports = {}, Op = Object.prototype, hasOwn = Op.hasOwnProperty, defineProperty = Object.defineProperty || function (obj, key, desc) { obj[key] = desc.value; }, $Symbol = "function" == typeof Symbol ? Symbol : {}, iteratorSymbol = $Symbol.iterator || "@@iterator", asyncIteratorSymbol = $Symbol.asyncIterator || "@@asyncIterator", toStringTagSymbol = $Symbol.toStringTag || "@@toStringTag"; function define(obj, key, value) { return Object.defineProperty(obj, key, { value: value, enumerable: !0, configurable: !0, writable: !0 }), obj[key]; } try { define({}, ""); } catch (err) { define = function define(obj, key, value) { return obj[key] = value; }; } function wrap(innerFn, outerFn, self, tryLocsList) { var protoGenerator = outerFn && outerFn.prototype instanceof Generator ? outerFn : Generator, generator = Object.create(protoGenerator.prototype), context = new Context(tryLocsList || []); return defineProperty(generator, "_invoke", { value: makeInvokeMethod(innerFn, self, context) }), generator; } function tryCatch(fn, obj, arg) { try { return { type: "normal", arg: fn.call(obj, arg) }; } catch (err) { return { type: "throw", arg: err }; } } exports.wrap = wrap; var ContinueSentinel = {}; function Generator() {} function GeneratorFunction() {} function GeneratorFunctionPrototype() {} var IteratorPrototype = {}; define(IteratorPrototype, iteratorSymbol, function () { return this; }); var getProto = Object.getPrototypeOf, NativeIteratorPrototype = getProto && getProto(getProto(values([]))); NativeIteratorPrototype && NativeIteratorPrototype !== Op && hasOwn.call(NativeIteratorPrototype, iteratorSymbol) && (IteratorPrototype = NativeIteratorPrototype); var Gp = GeneratorFunctionPrototype.prototype = Generator.prototype = Object.create(IteratorPrototype); function defineIteratorMethods(prototype) { ["next", "throw", "return"].forEach(function (method) { define(prototype, method, function (arg) { return this._invoke(method, arg); }); }); } function AsyncIterator(generator, PromiseImpl) { function invoke(method, arg, resolve, reject) { var record = tryCatch(generator[method], generator, arg); if ("throw" !== record.type) { var result = record.arg, value = result.value; return value && "object" == _typeof(value) && hasOwn.call(value, "__await") ? PromiseImpl.resolve(value.__await).then(function (value) { invoke("next", value, resolve, reject); }, function (err) { invoke("throw", err, resolve, reject); }) : PromiseImpl.resolve(value).then(function (unwrapped) { result.value = unwrapped, resolve(result); }, function (error) { return invoke("throw", error, resolve, reject); }); } reject(record.arg); } var previousPromise; defineProperty(this, "_invoke", { value: function value(method, arg) { function callInvokeWithMethodAndArg() { return new PromiseImpl(function (resolve, reject) { invoke(method, arg, resolve, reject); }); } return previousPromise = previousPromise ? previousPromise.then(callInvokeWithMethodAndArg, callInvokeWithMethodAndArg) : callInvokeWithMethodAndArg(); } }); } function makeInvokeMethod(innerFn, self, context) { var state = "suspendedStart"; return function (method, arg) { if ("executing" === state) throw new Error("Generator is already running"); if ("completed" === state) { if ("throw" === method) throw arg; return doneResult(); } for (context.method = method, context.arg = arg;;) { var delegate = context.delegate; if (delegate) { var delegateResult = maybeInvokeDelegate(delegate, context); if (delegateResult) { if (delegateResult === ContinueSentinel) continue; return delegateResult; } } if ("next" === context.method) context.sent = context._sent = context.arg;else if ("throw" === context.method) { if ("suspendedStart" === state) throw state = "completed", context.arg; context.dispatchException(context.arg); } else "return" === context.method && context.abrupt("return", context.arg); state = "executing"; var record = tryCatch(innerFn, self, context); if ("normal" === record.type) { if (state = context.done ? "completed" : "suspendedYield", record.arg === ContinueSentinel) continue; return { value: record.arg, done: context.done }; } "throw" === record.type && (state = "completed", context.method = "throw", context.arg = record.arg); } }; } function maybeInvokeDelegate(delegate, context) { var methodName = context.method, method = delegate.iterator[methodName]; if (undefined === method) return context.delegate = null, "throw" === methodName && delegate.iterator["return"] && (context.method = "return", context.arg = undefined, maybeInvokeDelegate(delegate, context), "throw" === context.method) || "return" !== methodName && (context.method = "throw", context.arg = new TypeError("The iterator does not provide a '" + methodName + "' method")), ContinueSentinel; var record = tryCatch(method, delegate.iterator, context.arg); if ("throw" === record.type) return context.method = "throw", context.arg = record.arg, context.delegate = null, ContinueSentinel; var info = record.arg; return info ? info.done ? (context[delegate.resultName] = info.value, context.next = delegate.nextLoc, "return" !== context.method && (context.method = "next", context.arg = undefined), context.delegate = null, ContinueSentinel) : info : (context.method = "throw", context.arg = new TypeError("iterator result is not an object"), context.delegate = null, ContinueSentinel); } function pushTryEntry(locs) { var entry = { tryLoc: locs[0] }; 1 in locs && (entry.catchLoc = locs[1]), 2 in locs && (entry.finallyLoc = locs[2], entry.afterLoc = locs[3]), this.tryEntries.push(entry); } function resetTryEntry(entry) { var record = entry.completion || {}; record.type = "normal", delete record.arg, entry.completion = record; } function Context(tryLocsList) { this.tryEntries = [{ tryLoc: "root" }], tryLocsList.forEach(pushTryEntry, this), this.reset(!0); } function values(iterable) { if (iterable) { var iteratorMethod = iterable[iteratorSymbol]; if (iteratorMethod) return iteratorMethod.call(iterable); if ("function" == typeof iterable.next) return iterable; if (!isNaN(iterable.length)) { var i = -1, next = function next() { for (; ++i < iterable.length;) if (hasOwn.call(iterable, i)) return next.value = iterable[i], next.done = !1, next; return next.value = undefined, next.done = !0, next; }; return next.next = next; } } return { next: doneResult }; } function doneResult() { return { value: undefined, done: !0 }; } return GeneratorFunction.prototype = GeneratorFunctionPrototype, defineProperty(Gp, "constructor", { value: GeneratorFunctionPrototype, configurable: !0 }), defineProperty(GeneratorFunctionPrototype, "constructor", { value: GeneratorFunction, configurable: !0 }), GeneratorFunction.displayName = define(GeneratorFunctionPrototype, toStringTagSymbol, "GeneratorFunction"), exports.isGeneratorFunction = function (genFun) { var ctor = "function" == typeof genFun && genFun.constructor; return !!ctor && (ctor === GeneratorFunction || "GeneratorFunction" === (ctor.displayName || ctor.name)); }, exports.mark = function (genFun) { return Object.setPrototypeOf ? Object.setPrototypeOf(genFun, GeneratorFunctionPrototype) : (genFun.__proto__ = GeneratorFunctionPrototype, define(genFun, toStringTagSymbol, "GeneratorFunction")), genFun.prototype = Object.create(Gp), genFun; }, exports.awrap = function (arg) { return { __await: arg }; }, defineIteratorMethods(AsyncIterator.prototype), define(AsyncIterator.prototype, asyncIteratorSymbol, function () { return this; }), exports.AsyncIterator = AsyncIterator, exports.async = function (innerFn, outerFn, self, tryLocsList, PromiseImpl) { void 0 === PromiseImpl && (PromiseImpl = Promise); var iter = new AsyncIterator(wrap(innerFn, outerFn, self, tryLocsList), PromiseImpl); return exports.isGeneratorFunction(outerFn) ? iter : iter.next().then(function (result) { return result.done ? result.value : iter.next(); }); }, defineIteratorMethods(Gp), define(Gp, toStringTagSymbol, "Generator"), define(Gp, iteratorSymbol, function () { return this; }), define(Gp, "toString", function () { return "[object Generator]"; }), exports.keys = function (val) { var object = Object(val), keys = []; for (var key in object) keys.push(key); return keys.reverse(), function next() { for (; keys.length;) { var key = keys.pop(); if (key in object) return next.value = key, next.done = !1, next; } return next.done = !0, next; }; }, exports.values = values, Context.prototype = { constructor: Context, reset: function reset(skipTempReset) { if (this.prev = 0, this.next = 0, this.sent = this._sent = undefined, this.done = !1, this.delegate = null, this.method = "next", this.arg = undefined, this.tryEntries.forEach(resetTryEntry), !skipTempReset) for (var name in this) "t" === name.charAt(0) && hasOwn.call(this, name) && !isNaN(+name.slice(1)) && (this[name] = undefined); }, stop: function stop() { this.done = !0; var rootRecord = this.tryEntries[0].completion; if ("throw" === rootRecord.type) throw rootRecord.arg; return this.rval; }, dispatchException: function dispatchException(exception) { if (this.done) throw exception; var context = this; function handle(loc, caught) { return record.type = "throw", record.arg = exception, context.next = loc, caught && (context.method = "next", context.arg = undefined), !!caught; } for (var i = this.tryEntries.length - 1; i >= 0; --i) { var entry = this.tryEntries[i], record = entry.completion; if ("root" === entry.tryLoc) return handle("end"); if (entry.tryLoc <= this.prev) { var hasCatch = hasOwn.call(entry, "catchLoc"), hasFinally = hasOwn.call(entry, "finallyLoc"); if (hasCatch && hasFinally) { if (this.prev < entry.catchLoc) return handle(entry.catchLoc, !0); if (this.prev < entry.finallyLoc) return handle(entry.finallyLoc); } else if (hasCatch) { if (this.prev < entry.catchLoc) return handle(entry.catchLoc, !0); } else { if (!hasFinally) throw new Error("try statement without catch or finally"); if (this.prev < entry.finallyLoc) return handle(entry.finallyLoc); } } } }, abrupt: function abrupt(type, arg) { for (var i = this.tryEntries.length - 1; i >= 0; --i) { var entry = this.tryEntries[i]; if (entry.tryLoc <= this.prev && hasOwn.call(entry, "finallyLoc") && this.prev < entry.finallyLoc) { var finallyEntry = entry; break; } } finallyEntry && ("break" === type || "continue" === type) && finallyEntry.tryLoc <= arg && arg <= finallyEntry.finallyLoc && (finallyEntry = null); var record = finallyEntry ? finallyEntry.completion : {}; return record.type = type, record.arg = arg, finallyEntry ? (this.method = "next", this.next = finallyEntry.finallyLoc, ContinueSentinel) : this.complete(record); }, complete: function complete(record, afterLoc) { if ("throw" === record.type) throw record.arg; return "break" === record.type || "continue" === record.type ? this.next = record.arg : "return" === record.type ? (this.rval = this.arg = record.arg, this.method = "return", this.next = "end") : "normal" === record.type && afterLoc && (this.next = afterLoc), ContinueSentinel; }, finish: function finish(finallyLoc) { for (var i = this.tryEntries.length - 1; i >= 0; --i) { var entry = this.tryEntries[i]; if (entry.finallyLoc === finallyLoc) return this.complete(entry.completion, entry.afterLoc), resetTryEntry(entry), ContinueSentinel; } }, "catch": function _catch(tryLoc) { for (var i = this.tryEntries.length - 1; i >= 0; --i) { var entry = this.tryEntries[i]; if (entry.tryLoc === tryLoc) { var record = entry.completion; if ("throw" === record.type) { var thrown = record.arg; resetTryEntry(entry); } return thrown; } } throw new Error("illegal catch attempt"); }, delegateYield: function delegateYield(iterable, resultName, nextLoc) { return this.delegate = { iterator: values(iterable), resultName: resultName, nextLoc: nextLoc }, "next" === this.method && (this.arg = undefined), ContinueSentinel; } }, exports; }
function asyncGeneratorStep(gen, resolve, reject, _next, _throw, key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { Promise.resolve(value).then(_next, _throw); } }
function _asyncToGenerator(fn) { return function () { var self = this, args = arguments; return new Promise(function (resolve, reject) { var gen = fn.apply(self, args); function _next(value) { asyncGeneratorStep(gen, resolve, reject, _next, _throw, "next", value); } function _throw(err) { asyncGeneratorStep(gen, resolve, reject, _next, _throw, "throw", err); } _next(undefined); }); }; }
var HttpHeaders = browser.webRequest.HttpHeaders;
var BASE_URL = 'https://kiran-research2.comminfo.rutgers.edu/data-collector-admin';
var INJECT_ITEMS_URL = BASE_URL + '/inject/';
var injectItems = [];
var startRank = 0;
var feed_collection_id = null;
var reels_collection_id = null;
var explore_collection_id = null;
function retrieveItems() {
  return _retrieveItems.apply(this, arguments);
}
function _retrieveItems() {
  _retrieveItems = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee2() {
    var response, data;
    return _regeneratorRuntime().wrap(function _callee2$(_context2) {
      while (1) switch (_context2.prev = _context2.next) {
        case 0:
          _context2.next = 2;
          return fetch(INJECT_ITEMS_URL);
        case 2:
          response = _context2.sent;
          _context2.next = 5;
          return response.json();
        case 5:
          data = _context2.sent;
          injectItems.splice(0, injectItems.length);
          injectItems.push.apply(injectItems, _toConsumableArray(data.items));
        case 8:
        case "end":
          return _context2.stop();
      }
    }, _callee2);
  }));
  return _retrieveItems.apply(this, arguments);
}
function removeCSP(_ref) {
  var responseHeaders = _ref.responseHeaders;
  var headersToRemove = ['content-security-policy', 'content-security-policy-report-only', 'cross-origin-embedder-policy-report-only', 'cross-origin-opener-policy', 'cross-origin-resource-policy', 'x-xss-protection', 'permissions-policy', 'document-policy'];
  var newHeaders = responseHeaders === null || responseHeaders === void 0 ? void 0 : responseHeaders.filter(function (header) {
    return !headersToRemove.includes(header.name.toLowerCase());
  });
  return {
    responseHeaders: newHeaders
  };
}
function interleaveEdges(edges) {
  var itemsToInjectCount = Math.min(injectItems.length, edges.length);
  console.log("itemsToInjectCount", itemsToInjectCount);
  var newEdges = [];
  var index;
  for (index = 0; index < itemsToInjectCount; index++) {
    newEdges.push(injectItems.shift());
    newEdges.push(edges[index]);
  }
  for (; index < edges.length; index++) {
    newEdges.push(edges[index]);
  }
  return newEdges;
}
function injectItemsInResponse(response) {
  var _response$data;
  if (response !== null && response !== void 0 && (_response$data = response.data) !== null && _response$data !== void 0 && _response$data.xdt_api__v1__feed__timeline__connection) {
    console.log('inject items', JSON.stringify(injectItems));
    if (injectItems.length === 0) {
      return;
    }
    var edges = response.data.xdt_api__v1__feed__timeline__connection.edges;
    response.data.xdt_api__v1__feed__timeline__connection.edges = interleaveEdges(edges);
    var newResponse = JSON.stringify(response);
    port.postMessage("feed: " + newResponse);
    return newResponse;
  }
}
function interceptResponseListener(_ref2) {
  var requestId = _ref2.requestId,
    url = _ref2.url;
  port.postMessage('url: ' + url);
  var filter = browser.webRequest.filterResponseData(requestId);
  var buffers = [];
  filter.ondata = function (event) {
    buffers.push(event.data);
  };
  filter.onstop = function (event) {
    var encoder = new TextEncoder();
    new Blob(buffers).text().then(function (text) {
      var body = JSON.parse(text);
      submitItems(body).then(function () {
        console.log('submitted items');
      })["catch"](function (error) {
        console.log('error in submitting items', error);
      });
      console.log('url', url);
      console.log('response', body);
      var newBody = injectItemsInResponse(body);
      if (newBody) {
        filter.write(encoder.encode(newBody));
      } else {
        for (var _i = 0, _buffers = buffers; _i < _buffers.length; _i++) {
          var buffer = _buffers[_i];
          filter.write(buffer);
        }
      }
      filter.disconnect();
    });
  };
  return {};
}
function submitNodes(_x, _x2) {
  return _submitNodes.apply(this, arguments);
}
function _submitNodes() {
  _submitNodes = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee3(source, nodes) {
    var _promise$survey_user_;
    var promise, survey_user_id, user_id, items, submitBody, submitResponse, submitJson;
    return _regeneratorRuntime().wrap(function _callee3$(_context3) {
      while (1) switch (_context3.prev = _context3.next) {
        case 0:
          _context3.next = 2;
          return browser.storage.local.get('survey_user_id');
        case 2:
          promise = _context3.sent;
          survey_user_id = (_promise$survey_user_ = promise === null || promise === void 0 ? void 0 : promise.survey_user_id) !== null && _promise$survey_user_ !== void 0 ? _promise$survey_user_ : "12345678";
          port.postMessage("survey user id: " + survey_user_id);
          _context3.next = 7;
          return browser.cookies.get({
            url: 'https://www.instagram.com/',
            name: 'ds_user_id'
          });
        case 7:
          user_id = _context3.sent;
          items = nodes.map(function (item, index) {
            return _objectSpread(_objectSpread({}, item), {}, {
              rank: index + startRank
            });
          });
          startRank += items.length;
          submitBody = {
            survey_user_id: survey_user_id,
            user_id: user_id === null || user_id === void 0 ? void 0 : user_id.value,
            source: source,
            type: 'active',
            items: items
          };
          if (source === 'feed' && feed_collection_id) {
            submitBody.collection_id = feed_collection_id;
          } else if (source === 'reels' && reels_collection_id) {
            submitBody.collection_id = reels_collection_id;
          } else if (source === 'explore' && explore_collection_id) {
            submitBody.collection_id = explore_collection_id;
          }
          console.log("submission", JSON.stringify(submitBody));
          _context3.next = 15;
          return fetch(BASE_URL + '/' + source + '/', {
            body: JSON.stringify(submitBody),
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            }
          });
        case 15:
          submitResponse = _context3.sent;
          _context3.next = 18;
          return submitResponse.json();
        case 18:
          submitJson = _context3.sent;
          if (submitResponse.ok) {
            _context3.next = 21;
            break;
          }
          throw new Error(JSON.stringify(submitJson));
        case 21:
          if (source === 'feed') {
            feed_collection_id = submitBody.collection_id;
          } else if (source === 'reels') {
            reels_collection_id = submitBody.collection_id;
          } else if (source === 'explore') {
            explore_collection_id = submitBody.collection_id;
          }
        case 22:
        case "end":
          return _context3.stop();
      }
    }, _callee3);
  }));
  return _submitNodes.apply(this, arguments);
}
function submitItems(_x3) {
  return _submitItems.apply(this, arguments);
}
function _submitItems() {
  _submitItems = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee4(response) {
    var _response$data2, _response$data3, _response$data4, _response$data5;
    var source, nodes;
    return _regeneratorRuntime().wrap(function _callee4$(_context4) {
      while (1) switch (_context4.prev = _context4.next) {
        case 0:
          source = null;
          nodes = [];
          if (response !== null && response !== void 0 && (_response$data2 = response.data) !== null && _response$data2 !== void 0 && _response$data2.xdt_api__v1__feed__timeline__connection) {
            source = 'feed';
            nodes = response.data.xdt_api__v1__feed__timeline__connection.edges.map(function (edge) {
              return edge.node;
            });
          } else if (response !== null && response !== void 0 && (_response$data3 = response.data) !== null && _response$data3 !== void 0 && _response$data3.xdt_api__v1__feed__home__connection) {
            source = 'feed';
            nodes = response.data.xdt_api__v1__feed__home__connection.edges.map(function (edge) {
              return edge.node;
            });
          } else if (response !== null && response !== void 0 && (_response$data4 = response.data) !== null && _response$data4 !== void 0 && _response$data4.xdt_api__v1__clips__home__connection) {
            source = 'reels';
            nodes = response.data.xdt_api__v1__clips__home__connection.edges.map(function (edge) {
              return edge.node;
            });
          } else if (response !== null && response !== void 0 && (_response$data5 = response.data) !== null && _response$data5 !== void 0 && _response$data5.xdt_api__v1__clips__home__connection_v2) {
            source = 'reels';
            nodes = response.data.xdt_api__v1__clips__home__connection_v2.edges.map(function (edge) {
              return edge.node;
            });
          } else if (response !== null && response !== void 0 && response.sectional_items) {
            port.postMessage('submission explore items');
            source = 'explore';
            nodes = response.sectional_items;
          }
          if (source) {
            _context4.next = 5;
            break;
          }
          return _context4.abrupt("return");
        case 5:
          _context4.next = 7;
          return submitNodes(source, nodes);
        case 7:
        case "end":
          return _context4.stop();
      }
    }, _callee4);
  }));
  return _submitItems.apply(this, arguments);
}
function prepareInject(_ref3) {
  var requestId = _ref3.requestId;
  var filter = browser.webRequest.filterResponseData(requestId);
  var buffers = [];
  console.log('starting retrieving inject items');
  var promise = retrieveItems().then(function () {
    port.postMessage('retrieved inject items' + injectItems);
  })["catch"](function (error) {
    port.postMessage('error while retrieving inject items' + error.message);
  });
  filter.ondata = function (event) {
    buffers.push(event.data);
  };
  filter.onstop = /*#__PURE__*/function () {
    var _ref4 = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee(event) {
      return _regeneratorRuntime().wrap(function _callee$(_context) {
        while (1) switch (_context.prev = _context.next) {
          case 0:
            promise["catch"](function (e) {
              return console.error(e);
            })["finally"](function () {
              var encoder = new TextEncoder();
              new Blob(buffers).text().then(function (text) {
                return injectPreload(text);
              }).then(function (newText) {
                filter.write(encoder.encode(newText));
                filter.disconnect();
              })["catch"](function (e) {
                for (var _i2 = 0, _buffers2 = buffers; _i2 < _buffers2.length; _i2++) {
                  var buffer = _buffers2[_i2];
                  filter.write(buffer);
                }
                filter.disconnect();
                console.error("error during preload inject", e);
                port.postMessage("error during preload inject " + e.message);
              });
            });
          case 1:
          case "end":
            return _context.stop();
        }
      }, _callee);
    }));
    return function (_x4) {
      return _ref4.apply(this, arguments);
    };
  }();
}
browser.webRequest.onHeadersReceived.addListener(removeCSP, {
  urls: ['<all_urls>']
}, ['blocking', 'responseHeaders']);
browser.webRequest.onBeforeRequest.addListener(prepareInject, {
  urls: ['https://www.instagram.com/']
}, ['blocking']);
browser.webRequest.onBeforeRequest.addListener(interceptResponseListener, {
  urls: ['https://www.instagram.com/graphql', 'https://www.instagram.com/graphql?*', 'https://www.instagram.com/graphql/query*', 'https://www.instagram.com/graphql/query?*', 'https://www.instagram.com/api/graphql', 'https://www.instagram.com/api/graphql?*', 'https://www.instagram.com/api/v1/discover/web/explore_grid*', 'https://www.instagram.com/api/v1/discover/web/explore_grid?*']
}, ['blocking']);
var port = browser.runtime.connectNative("browser");
port.onMessage.addListener(function (response) {
  if (response !== null && response !== void 0 && response.survey_user_id) {
    browser.storage.local.set({
      survey_user_id: response.survey_user_id
    }).then(function (r) {
      return console.log(r);
    });
  }
  port.postMessage("Received: ".concat(JSON.stringify(response)));
});
port.postMessage("Hello from WebExtension!");
function injectPreload(_x5) {
  return _injectPreload.apply(this, arguments);
}
function _injectPreload() {
  _injectPreload = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee5(html) {
    var parser, preloadDocument, scripts, _i3, _Array$from, _json$require, _json$require$, _json$require$$, _json$require$$$, _json$require$$$$__bb, _json$require$$$$__bb2, _json$require$$$$__bb3, _json$require$$$$__bb4, _json$require$$$$__bb5, _json$require$$$$__bb6, _json$require$$$$__bb7, _json$require$$$$__bb8, script, data, json, result, edges, nodes, newJson, scriptHtml, newHtml;
    return _regeneratorRuntime().wrap(function _callee5$(_context5) {
      while (1) switch (_context5.prev = _context5.next) {
        case 0:
          port.postMessage("Original HTML: " + html);
          parser = new DOMParser();
          preloadDocument = parser.parseFromString(html, "text/html");
          scripts = preloadDocument.querySelectorAll("script[data-sjs]");
          _i3 = 0, _Array$from = Array.from(scripts);
        case 5:
          if (!(_i3 < _Array$from.length)) {
            _context5.next = 35;
            break;
          }
          script = _Array$from[_i3];
          data = script.innerHTML;
          json = JSON.parse(data);
          result = json === null || json === void 0 ? void 0 : (_json$require = json.require) === null || _json$require === void 0 ? void 0 : (_json$require$ = _json$require[0]) === null || _json$require$ === void 0 ? void 0 : (_json$require$$ = _json$require$[3]) === null || _json$require$$ === void 0 ? void 0 : (_json$require$$$ = _json$require$$[0]) === null || _json$require$$$ === void 0 ? void 0 : (_json$require$$$$__bb = _json$require$$$.__bbox) === null || _json$require$$$$__bb === void 0 ? void 0 : (_json$require$$$$__bb2 = _json$require$$$$__bb.require) === null || _json$require$$$$__bb2 === void 0 ? void 0 : (_json$require$$$$__bb3 = _json$require$$$$__bb2[0]) === null || _json$require$$$$__bb3 === void 0 ? void 0 : (_json$require$$$$__bb4 = _json$require$$$$__bb3[3]) === null || _json$require$$$$__bb4 === void 0 ? void 0 : (_json$require$$$$__bb5 = _json$require$$$$__bb4[1]) === null || _json$require$$$$__bb5 === void 0 ? void 0 : (_json$require$$$$__bb6 = _json$require$$$$__bb5.__bbox) === null || _json$require$$$$__bb6 === void 0 ? void 0 : (_json$require$$$$__bb7 = _json$require$$$$__bb6.result) === null || _json$require$$$$__bb7 === void 0 ? void 0 : (_json$require$$$$__bb8 = _json$require$$$$__bb7.data) === null || _json$require$$$$__bb8 === void 0 ? void 0 : _json$require$$$$__bb8.xdt_api__v1__feed__timeline__connection;
          if (!result) {
            _context5.next = 32;
            break;
          }
          console.log("script found", script.innerHTML);
          edges = result.edges;
          nodes = edges.map(function (edge) {
            return edge.node;
          });
          _context5.prev = 14;
          _context5.next = 17;
          return submitNodes("feed", nodes);
        case 17:
          console.log('submitted items');
          _context5.next = 23;
          break;
        case 20:
          _context5.prev = 20;
          _context5.t0 = _context5["catch"](14);
          console.log('error in submitting items', _context5.t0);
        case 23:
          result.edges = interleaveEdges(edges);
          console.log("preload injected", result);
          newJson = JSON.stringify(json);
          scriptHtml = script;
          scriptHtml.dataset.contentLen = newJson.length.toString();
          script.innerHTML = newJson;
          newHtml = "<!DOCTYPE html>" + preloadDocument.documentElement.outerHTML;
          port.postMessage("Modified HTML: " + html);
          return _context5.abrupt("return", newHtml);
        case 32:
          _i3++;
          _context5.next = 5;
          break;
        case 35:
          return _context5.abrupt("return", html);
        case 36:
        case "end":
          return _context5.stop();
      }
    }, _callee5, null, [[14, 20]]);
  }));
  return _injectPreload.apply(this, arguments);
}
/******/ })()
;