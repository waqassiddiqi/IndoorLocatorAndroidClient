package org.redpin.android.core;

import org.redpin.android.db.LocalEntity;
import org.redpin.android.db.RemoteEntity;

public class User extends org.redpin.base.core.User implements
		RemoteEntity<Integer>, LocalEntity {

	protected Integer id;

	public Integer getRemoteId() {
		return id;
	}

	public void setRemoteId(Integer id) {
		this.id = id;
	}

	private transient long _id = -1;

	public long getLocalId() {
		return _id;
	}

	public void setLocalId(long id) {
		_id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (_id ^ (_id >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (_id != other._id)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

}