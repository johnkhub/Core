#!/bin/bash
keytool -printcert -sslserver pprd1.assetworld.co.za -rfc | keytool -import -noprompt -alias prod -keystore dtpw -storepass dtpw_store_password