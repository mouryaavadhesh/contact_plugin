import 'package:flutter_test/flutter_test.dart';
import 'package:contact_plugin/contact_plugin.dart';
import 'package:contact_plugin/contact_plugin_platform_interface.dart';
import 'package:contact_plugin/contact_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockContactPluginPlatform
    with MockPlatformInterfaceMixin
    implements ContactPluginPlatform {

  @override
  Future<List<Object>> getContacts() => Future.value([]);
}

void main() {
  final ContactPluginPlatform initialPlatform = ContactPluginPlatform.instance;

  test('$MethodChannelContactPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelContactPlugin>());
  });

  test('getPlatformVersion', () async {
    ContactPlugin contactPlugin = ContactPlugin();
    MockContactPluginPlatform fakePlatform = MockContactPluginPlatform();
    ContactPluginPlatform.instance = fakePlatform;

    expect(await contactPlugin.getContactList(), 0);
  });
}
