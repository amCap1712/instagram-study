/******/ (() => { // webpackBootstrap
var __webpack_exports__ = {};
setInterval(function () {
  var users = ["theonion", "tokyo_uyon", "beautifulslowlife"];
  for (var _i = 0, _users = users; _i < _users.length; _i++) {
    var user = _users[_i];
    document.querySelectorAll("a[href^='/".concat(user, "']")).forEach(function (element) {
      var item = element.closest("article");
      if (item) {
        item.style.borderStyle = "solid";
        item.style.borderWidth = "12px";
        item.style.borderColor = "red";
      }
    });
  }
}, 5000);
/******/ })()
;