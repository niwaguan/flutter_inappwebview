package com.pichillilorenzo.flutter_inappwebview_android.plugin_scripts_js;

import com.pichillilorenzo.flutter_inappwebview_android.types.PluginScript;
import com.pichillilorenzo.flutter_inappwebview_android.types.UserScriptInjectionTime;

public class RedirectAsyncAjaxRequestJS {
    public static final String REDIRECT_ASYNC_AJAX_REQUEST_JS_PLUGIN_SCRIPT_GROUP_NAME = "IN_APP_WEBVIEW_INTERCEPT_FETCH_REQUEST_JS_PLUGIN_SCRIPT";
    public static final PluginScript REDIRECT_ASYNC_AJAX_REQUEST_JS_PLUGIN_SCRIPT = new PluginScript(
            RedirectAsyncAjaxRequestJS.REDIRECT_ASYNC_AJAX_REQUEST_JS_PLUGIN_SCRIPT_GROUP_NAME,
            RedirectAsyncAjaxRequestJS.REDIRECT_ASYNC_AJAX_REQUEST_JS_SOURCE,
            UserScriptInjectionTime.AT_DOCUMENT_START,
            null,
            true,
            null
    );
    public static final String REDIRECT_ASYNC_AJAX_REQUEST_JS_SOURCE = "(function () {\n" +
            "  'use strict';\n" +
            "  var events = ['load', 'loadend', 'timeout', 'error', 'readystatechange', 'abort'];\n" +
            "  var OriginXhr = '__origin_xhr';\n" +
            "  function configEvent(event, xhrProxy) {\n" +
            "    var e = {};\n" +
            "    for (var attr in event) e[attr] = event[attr];\n" +
            "    e.target = e.currentTarget = xhrProxy;\n" +
            "    return e;\n" +
            "  }\n" +
            "  function hook(proxy, win) {\n" +
            "    win = win || window;\n" +
            "    var originXhr = win.XMLHttpRequest;\n" +
            "    var hooking = true;\n" +
            "    var HookXMLHttpRequest = function () {\n" +
            "      var xhr = new originXhr();\n" +
            "      for (var i = 0; i < events.length; ++i) {\n" +
            "        var key = 'on' + events[i];\n" +
            "        if (xhr[key] === undefined) xhr[key] = null;\n" +
            "      }\n" +
            "      for (var attr in xhr) {\n" +
            "        var type = '';\n" +
            "        try {\n" +
            "          type = typeof xhr[attr];\n" +
            "        } catch (e) {}\n" +
            "        if (type === 'function') {\n" +
            "          this[attr] = hookFunction(attr);\n" +
            "        } else if (attr !== OriginXhr) {\n" +
            "          Object.defineProperty(this, attr, {\n" +
            "            get: getterFactory(attr),\n" +
            "            set: setterFactory(attr),\n" +
            "            enumerable: true\n" +
            "          });\n" +
            "        }\n" +
            "      }\n" +
            "      var that = this;\n" +
            "      xhr.getProxy = function () {\n" +
            "        return that;\n" +
            "      };\n" +
            "      this[OriginXhr] = xhr;\n" +
            "    };\n" +
            "    HookXMLHttpRequest.prototype = originXhr.prototype;\n" +
            "    HookXMLHttpRequest.prototype.constructor = HookXMLHttpRequest;\n" +
            "    win.XMLHttpRequest = HookXMLHttpRequest;\n" +
            "    Object.assign(win.XMLHttpRequest, {\n" +
            "      UNSENT: 0,\n" +
            "      OPENED: 1,\n" +
            "      HEADERS_RECEIVED: 2,\n" +
            "      LOADING: 3,\n" +
            "      DONE: 4\n" +
            "    });\n" +
            "    function getterFactory(attr) {\n" +
            "      return function () {\n" +
            "        var originValue = this[OriginXhr][attr];\n" +
            "        if (hooking) {\n" +
            "          var v = this.hasOwnProperty(attr + '_') ? this[attr + '_'] : originValue;\n" +
            "          var attrGetterHook = (proxy[attr] || {})['getter'];\n" +
            "          return attrGetterHook && attrGetterHook(v, this) || v;\n" +
            "        } else {\n" +
            "          return originValue;\n" +
            "        }\n" +
            "      };\n" +
            "    }\n" +
            "    function setterFactory(attr) {\n" +
            "      return function (v) {\n" +
            "        var xhr = this[OriginXhr];\n" +
            "        if (hooking) {\n" +
            "          var that = this;\n" +
            "          var hook = proxy[attr];\n" +
            "          if (attr.substring(0, 2) === 'on') {\n" +
            "            that[attr + '_'] = v;\n" +
            "            xhr[attr] = function (e) {\n" +
            "              e = configEvent(e, that);\n" +
            "              var ret = proxy[attr] && proxy[attr].call(that, xhr, e);\n" +
            "              ret || v.call(that, e);\n" +
            "            };\n" +
            "          } else {\n" +
            "            var attrSetterHook = (hook || {})['setter'];\n" +
            "            v = attrSetterHook && attrSetterHook(v, that) || v;\n" +
            "            this[attr + '_'] = v;\n" +
            "            try {\n" +
            "              xhr[attr] = v;\n" +
            "            } catch (e) {}\n" +
            "          }\n" +
            "        } else {\n" +
            "          xhr[attr] = v;\n" +
            "        }\n" +
            "      };\n" +
            "    }\n" +
            "    function hookFunction(fun) {\n" +
            "      return function () {\n" +
            "        var args = [].slice.call(arguments);\n" +
            "        if (proxy[fun] && hooking) {\n" +
            "          var ret = proxy[fun].call(this, args, this[OriginXhr]);\n" +
            "          if (ret) return ret;\n" +
            "        }\n" +
            "        return this[OriginXhr][fun].apply(this[OriginXhr], args);\n" +
            "      };\n" +
            "    }\n" +
            "    function unHook() {\n" +
            "      hooking = false;\n" +
            "      if (win.XMLHttpRequest === HookXMLHttpRequest) {\n" +
            "        win.XMLHttpRequest = originXhr;\n" +
            "        HookXMLHttpRequest.prototype.constructor = originXhr;\n" +
            "        originXhr = undefined;\n" +
            "      }\n" +
            "    }\n" +
            "    return {\n" +
            "      originXhr,\n" +
            "      unHook\n" +
            "    };\n" +
            "  }\n" +
            "  var eventLoad = events[0],\n" +
            "    eventLoadEnd = events[1],\n" +
            "    eventTimeout = events[2],\n" +
            "    eventError = events[3],\n" +
            "    eventReadyStateChange = events[4],\n" +
            "    eventAbort = events[5];\n" +
            "  var prototype = 'prototype';\n" +
            "  function proxy(proxy, win) {\n" +
            "    win = win || window;\n" +
            "    return proxyAjax(proxy, win);\n" +
            "  }\n" +
            "  function trim(str) {\n" +
            "    return str.replace(/^\\s+|\\s+$/g, '');\n" +
            "  }\n" +
            "  function getEventTarget(xhr) {\n" +
            "    return xhr.watcher || (xhr.watcher = document.createElement('a'));\n" +
            "  }\n" +
            "  function triggerListener(xhr, name) {\n" +
            "    var xhrProxy = xhr.getProxy();\n" +
            "    var callback = 'on' + name + '_';\n" +
            "    var event = configEvent({\n" +
            "      type: name\n" +
            "    }, xhrProxy);\n" +
            "    xhrProxy[callback] && xhrProxy[callback](event);\n" +
            "    var evt;\n" +
            "    if (typeof Event === 'function') {\n" +
            "      evt = new Event(name, {\n" +
            "        bubbles: false\n" +
            "      });\n" +
            "    } else {\n" +
            "      evt = document.createEvent('Event');\n" +
            "      evt.initEvent(name, false, true);\n" +
            "    }\n" +
            "    getEventTarget(xhr).dispatchEvent(evt);\n" +
            "  }\n" +
            "  function Handler(xhr) {\n" +
            "    this.xhr = xhr;\n" +
            "    this.xhrProxy = xhr.getProxy();\n" +
            "  }\n" +
            "  Handler[prototype] = Object.create({\n" +
            "    resolve: function resolve(response) {\n" +
            "      var xhrProxy = this.xhrProxy;\n" +
            "      var xhr = this.xhr;\n" +
            "      xhrProxy.readyState = 4;\n" +
            "      xhr.resHeader = response.headers;\n" +
            "      xhrProxy.response = xhrProxy.responseText = response.response;\n" +
            "      xhrProxy.statusText = response.statusText;\n" +
            "      xhrProxy.status = response.status;\n" +
            "      triggerListener(xhr, eventReadyStateChange);\n" +
            "      triggerListener(xhr, eventLoad);\n" +
            "      triggerListener(xhr, eventLoadEnd);\n" +
            "    },\n" +
            "    reject: function reject(error) {\n" +
            "      this.xhrProxy.status = 0;\n" +
            "      triggerListener(this.xhr, error.type);\n" +
            "      triggerListener(this.xhr, eventLoadEnd);\n" +
            "    }\n" +
            "  });\n" +
            "  function makeHandler(next) {\n" +
            "    function sub(xhr) {\n" +
            "      Handler.call(this, xhr);\n" +
            "    }\n" +
            "    sub[prototype] = Object.create(Handler[prototype]);\n" +
            "    sub[prototype].next = next;\n" +
            "    return sub;\n" +
            "  }\n" +
            "  var RequestHandler = makeHandler(function (rq) {\n" +
            "    var xhr = this.xhr;\n" +
            "    rq = rq || xhr.config;\n" +
            "    xhr.withCredentials = rq.withCredentials;\n" +
            "    xhr.open(rq.method, rq.url, rq.async !== false, rq.user, rq.password);\n" +
            "    for (var key in rq.headers) {\n" +
            "      xhr.setRequestHeader(key, rq.headers[key]);\n" +
            "    }\n" +
            "    xhr.send(rq.body);\n" +
            "  });\n" +
            "  var ResponseHandler = makeHandler(function (response) {\n" +
            "    this.resolve(response);\n" +
            "  });\n" +
            "  var ErrorHandler = makeHandler(function (error) {\n" +
            "    this.reject(error);\n" +
            "  });\n" +
            "  function proxyAjax(proxy, win) {\n" +
            "    var onRequest = proxy.onRequest,\n" +
            "      onResponse = proxy.onResponse,\n" +
            "      onError = proxy.onError;\n" +
            "    function getResponseData(xhrProxy) {\n" +
            "      var responseType = xhrProxy.responseType;\n" +
            "      if (!responseType || responseType === 'text') {\n" +
            "        return xhrProxy.responseText;\n" +
            "      }\n" +
            "      var response = xhrProxy.response;\n" +
            "      if (responseType === 'json' && !response) {\n" +
            "        try {\n" +
            "          return JSON.parse(xhrProxy.responseText);\n" +
            "        } catch (e) {\n" +
            "          console.warn(e);\n" +
            "        }\n" +
            "      }\n" +
            "      return response;\n" +
            "    }\n" +
            "    function handleResponse(xhr, xhrProxy) {\n" +
            "      var handler = new ResponseHandler(xhr);\n" +
            "      var ret = {\n" +
            "        response: getResponseData(xhrProxy),\n" +
            "        status: xhrProxy.status,\n" +
            "        statusText: xhrProxy.statusText,\n" +
            "        config: xhr.config,\n" +
            "        headers: xhr.resHeader || xhr.getAllResponseHeaders().split('\\r\\n').reduce(function (ob, str) {\n" +
            "          if (str === '') return ob;\n" +
            "          var m = str.split(':');\n" +
            "          ob[m.shift()] = trim(m.join(':'));\n" +
            "          return ob;\n" +
            "        }, {})\n" +
            "      };\n" +
            "      if (!onResponse) return handler.resolve(ret);\n" +
            "      onResponse(ret, handler);\n" +
            "    }\n" +
            "    function onerror(xhr, xhrProxy, error, errorType) {\n" +
            "      var handler = new ErrorHandler(xhr);\n" +
            "      error = {\n" +
            "        config: xhr.config,\n" +
            "        error: error,\n" +
            "        type: errorType\n" +
            "      };\n" +
            "      if (onError) {\n" +
            "        onError(error, handler);\n" +
            "      } else {\n" +
            "        handler.next(error);\n" +
            "      }\n" +
            "    }\n" +
            "    function preventXhrProxyCallback() {\n" +
            "      return true;\n" +
            "    }\n" +
            "    function errorCallback(errorType) {\n" +
            "      return function (xhr, e) {\n" +
            "        onerror(xhr, this, e, errorType);\n" +
            "        return true;\n" +
            "      };\n" +
            "    }\n" +
            "    function stateChangeCallback(xhr, xhrProxy) {\n" +
            "      if (xhr.readyState === 4 && xhr.status !== 0) {\n" +
            "        handleResponse(xhr, xhrProxy);\n" +
            "      } else if (xhr.readyState !== 4) {\n" +
            "        triggerListener(xhr, eventReadyStateChange);\n" +
            "      }\n" +
            "      return true;\n" +
            "    }\n" +
            "    var {\n" +
            "      originXhr,\n" +
            "      unHook\n" +
            "    } = hook({\n" +
            "      onload: preventXhrProxyCallback,\n" +
            "      onloadend: preventXhrProxyCallback,\n" +
            "      onerror: errorCallback(eventError),\n" +
            "      ontimeout: errorCallback(eventTimeout),\n" +
            "      onabort: errorCallback(eventAbort),\n" +
            "      onreadystatechange: function (xhr) {\n" +
            "        return stateChangeCallback(xhr, this);\n" +
            "      },\n" +
            "      open: function open(args, xhr) {\n" +
            "        var _this = this;\n" +
            "        var config = xhr.config = {\n" +
            "          headers: {}\n" +
            "        };\n" +
            "        config.method = args[0];\n" +
            "        config.url = args[1];\n" +
            "        config.async = args[2];\n" +
            "        config.user = args[3];\n" +
            "        config.password = args[4];\n" +
            "        config.xhr = xhr;\n" +
            "        var evName = 'on' + eventReadyStateChange;\n" +
            "        if (!xhr[evName]) {\n" +
            "          xhr[evName] = function () {\n" +
            "            return stateChangeCallback(xhr, _this);\n" +
            "          };\n" +
            "        }\n" +
            "        if (onRequest) return true;\n" +
            "      },\n" +
            "      send: function (args, xhr) {\n" +
            "        var config = xhr.config;\n" +
            "        config.withCredentials = xhr.withCredentials;\n" +
            "        config.body = args[0];\n" +
            "        if (onRequest) {\n" +
            "          var req = function () {\n" +
            "            onRequest(config, new RequestHandler(xhr));\n" +
            "          };\n" +
            "          config.async === false ? req() : setTimeout(req);\n" +
            "          return true;\n" +
            "        }\n" +
            "      },\n" +
            "      setRequestHeader: function (args, xhr) {\n" +
            "        xhr.config.headers[args[0].toLowerCase()] = args[1];\n" +
            "        if (onRequest) return true;\n" +
            "      },\n" +
            "      addEventListener: function (args, xhr) {\n" +
            "        var _this = this;\n" +
            "        if (events.indexOf(args[0]) !== -1) {\n" +
            "          var handler = args[1];\n" +
            "          getEventTarget(xhr).addEventListener(args[0], function (e) {\n" +
            "            var event = configEvent(e, _this);\n" +
            "            event.type = args[0];\n" +
            "            event.isTrusted = true;\n" +
            "            handler.call(_this, event);\n" +
            "          });\n" +
            "          return true;\n" +
            "        }\n" +
            "      },\n" +
            "      getAllResponseHeaders: function (_, xhr) {\n" +
            "        var headers = xhr.resHeader;\n" +
            "        if (headers) {\n" +
            "          var header = '';\n" +
            "          for (var key in headers) {\n" +
            "            header += key + ': ' + headers[key] + '\\r\\n';\n" +
            "          }\n" +
            "          return header;\n" +
            "        }\n" +
            "      },\n" +
            "      getResponseHeader: function (args, xhr) {\n" +
            "        var headers = xhr.resHeader;\n" +
            "        if (headers) {\n" +
            "          return headers[(args[0] || '').toLowerCase()];\n" +
            "        }\n" +
            "      }\n" +
            "    }, win);\n" +
            "    return {\n" +
            "      originXhr,\n" +
            "      unProxy: unHook\n" +
            "    };\n" +
            "  }\n" +
            "  class FlutterXHRHookUtil {\n" +
            "    static convertArrayBufferToBase64(arraybuffer) {\n" +
            "      let uint8Array = new Uint8Array(arraybuffer);\n" +
            "      let charCode = '';\n" +
            "      let length = uint8Array.byteLength;\n" +
            "      for (let i = 0; i < length; i++) {\n" +
            "        charCode += String.fromCharCode(uint8Array[i]);\n" +
            "      }\n" +
            "      return window.btoa(charCode);\n" +
            "    }\n" +
            "    static convertFormDataToJson(formData, callback) {\n" +
            "      let allPromise = [];\n" +
            "      this.traversalEntries(formData, (key, value, fileName) => {\n" +
            "        allPromise.push(FlutterXHRHookUtil.convertSingleFormDataRecordToArray(key, value, fileName));\n" +
            "      });\n" +
            "      Promise.all(allPromise).then(formDatas => {\n" +
            "        let formDataJson = {};\n" +
            "        let formDataFileKeys = [];\n" +
            "        for (let i = 0; i < formDatas.length; i++) {\n" +
            "          let singleKeyValue = formDatas[i];\n" +
            "          if (singleKeyValue.length > 1 && !(typeof singleKeyValue[1] == 'string')) {\n" +
            "            formDataFileKeys.push(singleKeyValue[0]);\n" +
            "          }\n" +
            "        }\n" +
            "        formDataJson['fileKeys'] = formDataFileKeys;\n" +
            "        formDataJson['formData'] = formDatas;\n" +
            "        callback(formDataJson);\n" +
            "      }).catch(function (error) {\n" +
            "        console.log(error);\n" +
            "      });\n" +
            "    }\n" +
            "    static traversalEntries(formData, traversal) {\n" +
            "      if (formData._entries) {\n" +
            "        for (let i = 0; i < formData._entries.length; i++) {\n" +
            "          let pair = formData._entries[i];\n" +
            "          let key = pair[0];\n" +
            "          let value = pair[1];\n" +
            "          let fileName = pair.length > 2 ? pair[2] : null;\n" +
            "          if (traversal) {\n" +
            "            traversal(key, value, fileName);\n" +
            "          }\n" +
            "        }\n" +
            "      } else {\n" +
            "        for (let pair of formData.entries()) {\n" +
            "          let key = pair[0];\n" +
            "          let value = pair[1];\n" +
            "          if (traversal) {\n" +
            "            traversal(key, value, null);\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "    static convertSingleFormDataRecordToArray(key, value, fileName) {\n" +
            "      return new Promise((resolve, reject) => {\n" +
            "        let singleKeyValue = [];\n" +
            "        singleKeyValue.push(key);\n" +
            "        if (value instanceof File || value instanceof Blob) {\n" +
            "          let reader = new FileReader();\n" +
            "          reader.readAsDataURL(value);\n" +
            "          reader.onload = function (reader, ev) {\n" +
            "            let base64 = ev.target.result;\n" +
            "            let formDataFile = {\n" +
            "              name: fileName ? fileName : value instanceof File ? value.name : '',\n" +
            "              lastModified: value instanceof File ? value.lastModified : 0,\n" +
            "              size: value.size,\n" +
            "              type: value.type,\n" +
            "              data: base64\n" +
            "            };\n" +
            "            singleKeyValue.push(formDataFile);\n" +
            "            resolve(singleKeyValue);\n" +
            "            return null;\n" +
            "          };\n" +
            "          reader.onerror = function (reader) {\n" +
            "            reject(Error('formdata 表单读取文件数据失败'));\n" +
            "            return null;\n" +
            "          };\n" +
            "        } else {\n" +
            "          singleKeyValue.push(value);\n" +
            "          resolve(singleKeyValue);\n" +
            "        }\n" +
            "      });\n" +
            "    }\n" +
            "  }\n" +
            "  class FlutterXHRHook {\n" +
            "    static callHost(name, args) {\n" +
            "      return window.flutter_inappwebview.callHandler(name, args);\n" +
            "    }\n" +
            "    static shouldRedirectUrl(url) {\n" +
            "      if (url && window.flutter_inappwebview_xhr_hook_should_redirect && typeof window.flutter_inappwebview_xhr_hook_should_redirect == 'function') {\n" +
            "        return window.flutter_inappwebview_xhr_hook_should_redirect(url);\n" +
            "      }\n" +
            "      return false;\n" +
            "    }\n" +
            "    static convertBody(body, callback) {\n" +
            "      console.log('convertBody', body);\n" +
            "      if (body instanceof ArrayBuffer) {\n" +
            "        callback({\n" +
            "          type: 'ArrayBuffer',\n" +
            "          data: FlutterXHRHookUtil.convertArrayBufferToBase64(body)\n" +
            "        });\n" +
            "        return;\n" +
            "      }\n" +
            "      if (body instanceof Blob) {\n" +
            "        let fileReader = new FileReader();\n" +
            "        fileReader.onload = function (r, ev) {\n" +
            "          let base64 = ev.target.result;\n" +
            "          callback({\n" +
            "            type: 'Blob',\n" +
            "            data: base64\n" +
            "          });\n" +
            "        };\n" +
            "        fileReader.readAsDataURL(body);\n" +
            "        return;\n" +
            "      }\n" +
            "      if (body instanceof FormData) {\n" +
            "        FlutterXHRHookUtil.convertFormDataToJson(body, json => {\n" +
            "          callback({\n" +
            "            type: 'multipart/form-data',\n" +
            "            data: json\n" +
            "          });\n" +
            "        });\n" +
            "        return;\n" +
            "      }\n" +
            "      callback({\n" +
            "        type: 'String',\n" +
            "        data: body\n" +
            "      });\n" +
            "    }\n" +
            "    static init() {\n" +
            "      console.log('xhr hook inited');\n" +
            "      proxy({\n" +
            "        onRequest: (config, handler) => {\n" +
            "          if (FlutterXHRHook.shouldRedirectUrl(config.url)) {\n" +
            "            this.convertBody(config.body, ({\n" +
            "              data,\n" +
            "              type\n" +
            "            }) => {\n" +
            "              console.log('onRedirectAsyncAjaxRequest', data, type);\n" +
            "              config.headers = config.headers || {};\n" +
            "              config.headers['User-Agent'] = navigator.userAgent;\n" +
            "              config.headers['Accept-Language'] = navigator.language;\n" +
            "              config.headers['Origin'] = location.origin;\n" +
            "              config.headers['Referer'] = document.referrer;\n" +
            "              config.headers['Host'] = location.host;\n" +
            "              config.headers['X-Body-Type'] = type;\n" +
            "              const request = {\n" +
            "                data,\n" +
            "                method: config.method,\n" +
            "                url: config.url,\n" +
            "                isAsync: config.async,\n" +
            "                user: config.user,\n" +
            "                password: config.password,\n" +
            "                withCredentials: config.withCredentials,\n" +
            "                headers: config.headers,\n" +
            "                responseType: config.responseType\n" +
            "              };\n" +
            "              this.callHost('onRedirectAsyncAjaxRequest', request).then(ajaxResponse => {\n" +
            "                console.log('onRedirectAsyncAjaxRequest response', ajaxResponse);\n" +
            "                if (ajaxResponse) {\n" +
            "                  handler.resolve({\n" +
            "                    config: config,\n" +
            "                    status: ajaxResponse.status,\n" +
            "                    headers: ajaxResponse.responseHeaders,\n" +
            "                    response: ajaxResponse.response\n" +
            "                  });\n" +
            "                } else {\n" +
            "                  handler.resolve({\n" +
            "                    config: config,\n" +
            "                    status: 500\n" +
            "                  });\n" +
            "                }\n" +
            "              });\n" +
            "              console.log('onRequest', request);\n" +
            "            });\n" +
            "            return;\n" +
            "          }\n" +
            "          handler.next(config);\n" +
            "        }\n" +
            "      });\n" +
            "    }\n" +
            "  }\n" +
            "  FlutterXHRHook.init();\n" +
            "})();";
}
