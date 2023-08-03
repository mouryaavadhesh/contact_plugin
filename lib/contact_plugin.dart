
import 'contact_plugin_platform_interface.dart';

class ContactPlugin {

  Future<dynamic> getContactList() {

    return ContactPluginPlatform.instance.getContacts();
  }
}
