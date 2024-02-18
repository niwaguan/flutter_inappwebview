import 'dart:async';

import 'ajax_request.dart';

abstract class AsyncAjaxRequestRedirector {
  /// send a request
  FutureOr<AjaxRequest?> redirectRequest(AjaxRequest request);
}
