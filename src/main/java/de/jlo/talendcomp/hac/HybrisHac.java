/**
 * Copyright 2021 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This project is based of the jhac implementation of Klaus Hausschild
 * https://github.com/klaushauschild1984/jhac
 */
package de.jlo.talendcomp.hac;
import com.sap.hybris.hac.Configuration;

import com.sap.hybris.hac.Credentials;
import com.sap.hybris.hac.HybrisAdministrationConsole;

public class HybrisHac {
	
	protected HybrisAdministrationConsole console = null;
	private Configuration config = null;
	public static final String TEST_FS_QUERY = "SELECT count(0) as test FROM {Product} where 1=0";
	public static final int TEST_FS_QUERY_MAX_COUNT = 1;
	private int timeout = 0;
	
	public void connect(String hacUrl, String hacUser, String hacPassword, String htAccessUser, String htAccessPassword) throws Exception {
		if (isEmpty(hacUrl)) {
			throw new IllegalArgumentException("HAC endpoint cannot be null or empty");
		}
		if (isEmpty(hacUser)) {
			throw new IllegalArgumentException("HAC user cannot be null or empty");
		}
		if (isEmpty(hacPassword)) {
			throw new IllegalArgumentException("HAC password cannot be null or empty");
		}
		hacUrl = hacUrl.trim();
		if (hacUrl.endsWith("/")) {
			hacUrl = hacUrl.substring(0, hacUrl.length() - 1);
		}
		hacUser = hacUser.trim();
		hacPassword = hacPassword.trim();
		htAccessUser = htAccessUser.trim();
		htAccessPassword = htAccessPassword.trim();
		Credentials htAccess = null;
		if (htAccessUser != null && htAccessUser.trim().isEmpty() == false) {
			if (htAccessPassword == null || htAccessPassword.trim().isEmpty()) {
				throw new IllegalArgumentException("Htaccess password cannot be blank if htaccess user is given");
			}
			htAccess = Credentials._builder().username(htAccessUser).password(htAccessPassword).build();
		}
		config = Configuration
					.builder()
					.endpoint(hacUrl)
					.username(hacUser)
					.password(hacPassword)
					.htaccess(htAccess)
					.timeout(timeout)
					.build();
		try {
			console = HybrisAdministrationConsole.hac(config);
		} catch (Exception e) {
			throw new Exception("Connect to endpoint: " + hacUrl + " with user: " + hacUser + " failed: " + e, e);
		}
	}
	
	/**
	 * returns true if the string is null or empty or equals "null"
	 * @param s the string
	 * @returns true if empty 
	 */
	public static boolean isEmpty(String s) {
		if (s == null) {
			return true;
		}
		if (s.trim().isEmpty()) {
			return true;
		}
		if (s.trim().equalsIgnoreCase("null")) {
			return true;
		}
		return false;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		if (timeout != null) {
			this.timeout = timeout;
		}
	}

}
