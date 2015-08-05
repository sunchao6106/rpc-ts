package com.sunchao.rpc.common.status.support;

import java.util.Map;

import com.sunchao.rpc.common.status.Status;
import com.sunchao.rpc.common.status.Status.Level;



/**
 * StatusManager.
 * @author sunchao
 *
 */
public class StatusHelper {

	public static Status getSummargStatus(Map<String, Status> statuses) {
		Level level = Level.OK;
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Status> entry : statuses.entrySet()) {
			String key = entry.getKey(); 
			Status status = entry.getValue();
			Level l = status.getLevel();
			if (Level.ERROR.equals(l)) {
				level = Level.ERROR;
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(key);
			} else if (Level.WARN.equals(l)) {
				if (! Level.ERROR.equals(level)) {
					level = Level.WARN;
				}
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(key);
			}
		}
		return new Status(level, sb.toString());
	}
}
