setInterval(function () {
  const users = ["theonion", "tokyo_uyon", "beautifulslowlife"];
  for (const user of users) {
    document
      .querySelectorAll(`a[href^='/${user}']`)
      .forEach(function (element) {
        const item = element.closest("article");
        if (item) {
          item.style.borderStyle = "solid";
          item.style.borderWidth = "12px";
          item.style.borderColor = "red";
        }
      });
  }
}, 5000);
