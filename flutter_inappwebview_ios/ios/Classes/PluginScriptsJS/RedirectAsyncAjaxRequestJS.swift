//
//  RedirectAsyncAjaxJS.swift
//  flutter_inappwebview_ios
//
//  Created by 高洋 on 2024/2/12.
//

import Foundation

func createRedirectAsyncAjaxRequestPluginScript() -> PluginScript {
    return PluginScript(groupName: INTERCEPT_AJAX_REQUEST_JS_PLUGIN_SCRIPT_GROUP_NAME,
        source: REDIRECT_ASYNC_AJAX_REQUEST_JS_SOURCE,
        injectionTime: .atDocumentStart,
        forMainFrameOnly: false,
        requiredInAllContentWorlds: true,
        messageHandlerNames: []
    );
}

let REDIRECT_ASYNC_AJAX_REQUEST_JS_SOURCE = """
(function () {
  'use strict';

  var events = ['load', 'loadend', 'timeout', 'error', 'readystatechange', 'abort'];
  var OriginXhr = '__origin_xhr';
  function configEvent(event, xhrProxy) {
    var e = {};
    for (var attr in event) e[attr] = event[attr];
    e.target = e.currentTarget = xhrProxy;
    return e;
  }
  function hook(proxy, win) {
    win = win || window;
    var originXhr = win.XMLHttpRequest;
    var hooking = true;
    var HookXMLHttpRequest = function () {
      var xhr = new originXhr();
      for (var i = 0; i < events.length; ++i) {
        var key = 'on' + events[i];
        if (xhr[key] === undefined) xhr[key] = null;
      }
      for (var attr in xhr) {
        var type = "";
        try {
          type = typeof xhr[attr];
        } catch (e) {}
        if (type === "function") {
          this[attr] = hookFunction(attr);
        } else if (attr !== OriginXhr) {
          Object.defineProperty(this, attr, {
            get: getterFactory(attr),
            set: setterFactory(attr),
            enumerable: true
          });
        }
      }
      var that = this;
      xhr.getProxy = function () {
        return that;
      };
      this[OriginXhr] = xhr;
    };
    HookXMLHttpRequest.prototype = originXhr.prototype;
    HookXMLHttpRequest.prototype.constructor = HookXMLHttpRequest;
    win.XMLHttpRequest = HookXMLHttpRequest;
    Object.assign(win.XMLHttpRequest, {
      UNSENT: 0,
      OPENED: 1,
      HEADERS_RECEIVED: 2,
      LOADING: 3,
      DONE: 4
    });
    function getterFactory(attr) {
      return function () {
        var originValue = this[OriginXhr][attr];
        if (hooking) {
          var v = this.hasOwnProperty(attr + "_") ? this[attr + "_"] : originValue;
          var attrGetterHook = (proxy[attr] || {})["getter"];
          return attrGetterHook && attrGetterHook(v, this) || v;
        } else {
          return originValue;
        }
      };
    }
    function setterFactory(attr) {
      return function (v) {
        var xhr = this[OriginXhr];
        if (hooking) {
          var that = this;
          var hook = proxy[attr];
          if (attr.substring(0, 2) === 'on') {
            that[attr + "_"] = v;
            xhr[attr] = function (e) {
              e = configEvent(e, that);
              var ret = proxy[attr] && proxy[attr].call(that, xhr, e);
              ret || v.call(that, e);
            };
          } else {
            var attrSetterHook = (hook || {})["setter"];
            v = attrSetterHook && attrSetterHook(v, that) || v;
            this[attr + "_"] = v;
            try {
              xhr[attr] = v;
            } catch (e) {}
          }
        } else {
          xhr[attr] = v;
        }
      };
    }
    function hookFunction(fun) {
      return function () {
        var args = [].slice.call(arguments);
        if (proxy[fun] && hooking) {
          var ret = proxy[fun].call(this, args, this[OriginXhr]);
          if (ret) return ret;
        }
        return this[OriginXhr][fun].apply(this[OriginXhr], args);
      };
    }
    function unHook() {
      hooking = false;
      if (win.XMLHttpRequest === HookXMLHttpRequest) {
        win.XMLHttpRequest = originXhr;
        HookXMLHttpRequest.prototype.constructor = originXhr;
        originXhr = undefined;
      }
    }
    return {
      originXhr,
      unHook
    };
  }

  var eventLoad = events[0],
    eventLoadEnd = events[1],
    eventTimeout = events[2],
    eventError = events[3],
    eventReadyStateChange = events[4],
    eventAbort = events[5];
  var prototype = "prototype";
  function proxy(proxy, win) {
    win = win || window;
    return proxyAjax(proxy, win);
  }
  function trim(str) {
    return str.replace(/^\\s+|\\s+$/g, "");
  }
  function getEventTarget(xhr) {
    return xhr.watcher || (xhr.watcher = document.createElement("a"));
  }
  function triggerListener(xhr, name) {
    var xhrProxy = xhr.getProxy();
    var callback = "on" + name + "_";
    var event = configEvent({
      type: name
    }, xhrProxy);
    xhrProxy[callback] && xhrProxy[callback](event);
    var evt;
    if (typeof Event === "function") {
      evt = new Event(name, {
        bubbles: false
      });
    } else {
      evt = document.createEvent("Event");
      evt.initEvent(name, false, true);
    }
    getEventTarget(xhr).dispatchEvent(evt);
  }
  function Handler(xhr) {
    this.xhr = xhr;
    this.xhrProxy = xhr.getProxy();
  }
  Handler[prototype] = Object.create({
    resolve: function resolve(response) {
      var xhrProxy = this.xhrProxy;
      var xhr = this.xhr;
      xhrProxy.readyState = 4;
      xhr.resHeader = response.headers;
      xhrProxy.response = xhrProxy.responseText = response.response;
      xhrProxy.statusText = response.statusText;
      xhrProxy.status = response.status;
      triggerListener(xhr, eventReadyStateChange);
      triggerListener(xhr, eventLoad);
      triggerListener(xhr, eventLoadEnd);
    },
    reject: function reject(error) {
      this.xhrProxy.status = 0;
      triggerListener(this.xhr, error.type);
      triggerListener(this.xhr, eventLoadEnd);
    }
  });
  function makeHandler(next) {
    function sub(xhr) {
      Handler.call(this, xhr);
    }
    sub[prototype] = Object.create(Handler[prototype]);
    sub[prototype].next = next;
    return sub;
  }
  var RequestHandler = makeHandler(function (rq) {
    var xhr = this.xhr;
    rq = rq || xhr.config;
    xhr.withCredentials = rq.withCredentials;
    xhr.open(rq.method, rq.url, rq.async !== false, rq.user, rq.password);
    for (var key in rq.headers) {
      xhr.setRequestHeader(key, rq.headers[key]);
    }
    xhr.send(rq.body);
  });
  var ResponseHandler = makeHandler(function (response) {
    this.resolve(response);
  });
  var ErrorHandler = makeHandler(function (error) {
    this.reject(error);
  });
  function proxyAjax(proxy, win) {
    var onRequest = proxy.onRequest,
      onResponse = proxy.onResponse,
      onError = proxy.onError;
    function getResponseData(xhrProxy) {
      var responseType = xhrProxy.responseType;
      if (!responseType || responseType === "text") {
        return xhrProxy.responseText;
      }
      var response = xhrProxy.response;
      if (responseType === "json" && !response) {
        try {
          return JSON.parse(xhrProxy.responseText);
        } catch (e) {
          console.warn(e);
        }
      }
      return response;
    }
    function handleResponse(xhr, xhrProxy) {
      var handler = new ResponseHandler(xhr);
      var ret = {
        response: getResponseData(xhrProxy),
        status: xhrProxy.status,
        statusText: xhrProxy.statusText,
        config: xhr.config,
        headers: xhr.resHeader || xhr.getAllResponseHeaders().split("\\r\\n").reduce(function (ob, str) {
          if (str === "") return ob;
          var m = str.split(":");
          ob[m.shift()] = trim(m.join(":"));
          return ob;
        }, {})
      };
      if (!onResponse) return handler.resolve(ret);
      onResponse(ret, handler);
    }
    function onerror(xhr, xhrProxy, error, errorType) {
      var handler = new ErrorHandler(xhr);
      error = {
        config: xhr.config,
        error: error,
        type: errorType
      };
      if (onError) {
        onError(error, handler);
      } else {
        handler.next(error);
      }
    }
    function preventXhrProxyCallback() {
      return true;
    }
    function errorCallback(errorType) {
      return function (xhr, e) {
        onerror(xhr, this, e, errorType);
        return true;
      };
    }
    function stateChangeCallback(xhr, xhrProxy) {
      if (xhr.readyState === 4 && xhr.status !== 0) {
        handleResponse(xhr, xhrProxy);
      } else if (xhr.readyState !== 4) {
        triggerListener(xhr, eventReadyStateChange);
      }
      return true;
    }
    var {
      originXhr,
      unHook
    } = hook({
      onload: preventXhrProxyCallback,
      onloadend: preventXhrProxyCallback,
      onerror: errorCallback(eventError),
      ontimeout: errorCallback(eventTimeout),
      onabort: errorCallback(eventAbort),
      onreadystatechange: function (xhr) {
        return stateChangeCallback(xhr, this);
      },
      open: function open(args, xhr) {
        var _this = this;
        var config = xhr.config = {
          headers: {}
        };
        config.method = args[0];
        config.url = args[1];
        config.async = args[2];
        config.user = args[3];
        config.password = args[4];
        config.xhr = xhr;
        var evName = "on" + eventReadyStateChange;
        if (!xhr[evName]) {
          xhr[evName] = function () {
            return stateChangeCallback(xhr, _this);
          };
        }
        if (onRequest) return true;
      },
      send: function (args, xhr) {
        var config = xhr.config;
        config.withCredentials = xhr.withCredentials;
        config.body = args[0];
        if (onRequest) {
          var req = function () {
            onRequest(config, new RequestHandler(xhr));
          };
          config.async === false ? req() : setTimeout(req);
          return true;
        }
      },
      setRequestHeader: function (args, xhr) {
        xhr.config.headers[args[0].toLowerCase()] = args[1];
        if (onRequest) return true;
      },
      addEventListener: function (args, xhr) {
        var _this = this;
        if (events.indexOf(args[0]) !== -1) {
          var handler = args[1];
          getEventTarget(xhr).addEventListener(args[0], function (e) {
            var event = configEvent(e, _this);
            event.type = args[0];
            event.isTrusted = true;
            handler.call(_this, event);
          });
          return true;
        }
      },
      getAllResponseHeaders: function (_, xhr) {
        var headers = xhr.resHeader;
        if (headers) {
          var header = "";
          for (var key in headers) {
            header += key + ": " + headers[key] + "\\r\\n";
          }
          return header;
        }
      },
      getResponseHeader: function (args, xhr) {
        var headers = xhr.resHeader;
        if (headers) {
          return headers[(args[0] || "").toLowerCase()];
        }
      }
    }, win);
    return {
      originXhr,
      unProxy: unHook
    };
  }

  class FlutterXHRHookUtil {
    static convertArrayBufferToBase64(arraybuffer) {
      let uint8Array = new Uint8Array(arraybuffer);
      let charCode = "";
      let length = uint8Array.byteLength;
      for (let i = 0; i < length; i++) {
        charCode += String.fromCharCode(uint8Array[i]);
      }
      return window.btoa(charCode);
    }
    static convertFormDataToJson(formData, callback) {
      let allPromise = [];
      this.traversalEntries(formData, (key, value, fileName) => {
        allPromise.push(FlutterXHRHookUtil.convertSingleFormDataRecordToArray(key, value, fileName));
      });
      Promise.all(allPromise).then(formDatas => {
        let formDataJson = {};
        let formDataFileKeys = [];
        for (let i = 0; i < formDatas.length; i++) {
          let singleKeyValue = formDatas[i];
          if (singleKeyValue.length > 1 && !(typeof singleKeyValue[1] == "string")) {
            formDataFileKeys.push(singleKeyValue[0]);
          }
        }
        formDataJson["fileKeys"] = formDataFileKeys;
        formDataJson["formData"] = formDatas;
        callback(formDataJson);
      }).catch(function (error) {
        console.log(error);
      });
    }
    static traversalEntries(formData, traversal) {
      if (formData._entries) {
        for (let i = 0; i < formData._entries.length; i++) {
          let pair = formData._entries[i];
          let key = pair[0];
          let value = pair[1];
          let fileName = pair.length > 2 ? pair[2] : null;
          if (traversal) {
            traversal(key, value, fileName);
          }
        }
      } else {
        for (let pair of formData.entries()) {
          let key = pair[0];
          let value = pair[1];
          if (traversal) {
            traversal(key, value, null);
          }
        }
      }
    }
    static convertSingleFormDataRecordToArray(key, value, fileName) {
      return new Promise((resolve, reject) => {
        let singleKeyValue = [];
        singleKeyValue.push(key);
        if (value instanceof File || value instanceof Blob) {
          let reader = new FileReader();
          reader.readAsDataURL(value);
          reader.onload = function (reader, ev) {
            let base64 = ev.target.result;
            let formDataFile = {
              name: fileName ? fileName : value instanceof File ? value.name : "",
              lastModified: value instanceof File ? value.lastModified : 0,
              size: value.size,
              type: value.type,
              data: base64
            };
            singleKeyValue.push(formDataFile);
            resolve(singleKeyValue);
            return null;
          };
          reader.onerror = function (reader) {
            reject(Error("formdata 表单读取文件数据失败"));
            return null;
          };
        } else {
          singleKeyValue.push(value);
          resolve(singleKeyValue);
        }
      });
    }
  }
  class FlutterXHRHook {
    static callHost(name, args) {
      return window.flutter_inappwebview.callHandler(name, args);
    }
    static shouldRedirectUrl(url) {
      if (url && window.flutter_inappwebview_xhr_hook_should_redirect && typeof window.flutter_inappwebview_xhr_hook_should_redirect == "function") {
        return window.flutter_inappwebview_xhr_hook_should_redirect(url);
      }
      return false;
    }
    static convertBody(body, callback) {
      console.log("convertBody", body);
      if (body instanceof ArrayBuffer) {
        callback({
          type: "ArrayBuffer",
          data: FlutterXHRHookUtil.convertArrayBufferToBase64(body)
        });
        return;
      }
      if (body instanceof Blob) {
        let fileReader = new FileReader();
        fileReader.onload = function (r, ev) {
          let base64 = ev.target.result;
          callback({
            type: "Blob",
            data: base64
          });
        };
        fileReader.readAsDataURL(body);
        return;
      }
      if (body instanceof FormData) {
        FlutterXHRHookUtil.convertFormDataToJson(body, json => {
          callback({
            type: "multipart/form-data",
            data: json
          });
        });
        return;
      }
      callback({
        type: "String",
        data: body
      });
    }
    static init() {
      console.log("xhr hook inited");
      proxy({
        onRequest: (config, handler) => {
          if (FlutterXHRHook.shouldRedirectUrl(config.url)) {
            this.convertBody(config.body, ({
              data,
              type
            }) => {
              console.log("onRedirectAsyncAjaxRequest", data, type);
              config.headers = config.headers || {};
              config.headers["User-Agent"] = navigator.userAgent;
              config.headers["Accept-Language"] = navigator.language;
              config.headers["Origin"] = location.origin;
              config.headers["Referer"] = document.referrer;
              config.headers["Host"] = location.host;
              config.headers["X-Body-Type"] = type;
              const request = {
                data,
                method: config.method,
                url: config.url,
                isAsync: config.async,
                user: config.user,
                password: config.password,
                withCredentials: config.withCredentials,
                headers: config.headers,
                responseType: config.responseType
              };
              this.callHost("onRedirectAsyncAjaxRequest", request).then(ajaxResponse => {
                console.log("onRedirectAsyncAjaxRequest response", ajaxResponse);
                if (ajaxResponse) {
                  handler.resolve({
                    config: config,
                    status: ajaxResponse.status,
                    headers: ajaxResponse.responseHeaders,
                    response: ajaxResponse.response
                  });
                } else {
                  handler.resolve({
                    config: config,
                    status: 500
                  });
                }
              });
              console.log("onRequest", request);
            });
            return;
          }
          handler.next(config);
        }
      });
    }
  }
  FlutterXHRHook.init();

})();

"""
