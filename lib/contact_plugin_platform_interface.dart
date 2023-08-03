import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'contact_plugin_method_channel.dart';

abstract class ContactPluginPlatform extends PlatformInterface {
  /// Constructs a ContactPluginPlatform.
  ContactPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static ContactPluginPlatform _instance = MethodChannelContactPlugin();

  /// The default instance of [ContactPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelContactPlugin].
  static ContactPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ContactPluginPlatform] when
  /// they register themselves.
  static set instance(ContactPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<List<Object>> getContacts() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
