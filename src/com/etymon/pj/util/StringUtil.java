package com.etymon.pj.util;

public final class StringUtil {

	public static String sprintf(String template, String[] args) {
		if (args == null) {
			return template;
		}
		StringBuffer sb = new StringBuffer();
		int start = 0;
		int index;
		int x = 0;
		while ( (x < args.length) && ((index =
					       template.indexOf("%s",
								start)) != -1) ) {
			// allow \%s to escape
			if ( (index == 0) || (template.charAt(index -
							      1) !=
					      '\\') ) {
				sb.append(template.substring(start,
							     index));
				sb.append(args[x]);
				start = index + 2;
			}
		}
		sb.append(template.substring(start));
		return sb.toString();
	}

}
