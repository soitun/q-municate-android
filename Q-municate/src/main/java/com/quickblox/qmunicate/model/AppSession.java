package com.quickblox.qmunicate.model;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.PrefsHelper;

import java.io.Serializable;

public class AppSession implements Serializable {

    private static AppSession activeSession;
    private final LoginType loginType;
    private static final Object lock = new Object();
    private QBUser user;

    private AppSession(LoginType loginType, QBUser user) {
        this.loginType = loginType;
        this.user = user;
        save();
    }

    public static void startSession(LoginType loginType, QBUser user) {
        activeSession = new AppSession(loginType, user);
    }

    public void closeAndClear() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        helper.delete(PrefsHelper.PREF_USER_EMAIL);
        helper.delete(PrefsHelper.PREF_LOGIN_TYPE);
        helper.delete(PrefsHelper.PREF_USER_ID);
        activeSession = null;
    }

    public QBUser getUser() {
        return user;
    }

    public void save() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        prefsHelper.savePref(PrefsHelper.PREF_LOGIN_TYPE, loginType.toString());
        saveUser(user, prefsHelper);
    }

    public static AppSession getActiveSession() {
        synchronized (lock) {
            return activeSession;
        }
    }

    public void updateUser(QBUser user) {
        this.user = user;
        saveUser(this.user, App.getInstance().getPrefsHelper());
    }

    private void saveUser(QBUser user, PrefsHelper prefsHelper) {
        prefsHelper.savePref(PrefsHelper.PREF_USER_ID, user.getId());
        prefsHelper.savePref(PrefsHelper.PREF_USER_FULL_NAME, user.getFullName());
    }

    public boolean isSessionExist() {
        return loginType != null && user.getId() != Consts.NOT_INITIALIZED_VALUE;
    }

    public LoginType getLoginType() {
        return loginType;
    }

    public static AppSession load() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        String loginTypeRaw = helper.getPref(PrefsHelper.PREF_LOGIN_TYPE, LoginType.EMAIL.toString());
        int userId = helper.getPref(PrefsHelper.PREF_USER_ID, Consts.NOT_INITIALIZED_VALUE);
        String userFullName = helper.getPref(PrefsHelper.PREF_USER_FULL_NAME, Consts.EMPTY_STRING);
        QBUser qbUser = new QBUser();
        qbUser.setId(userId);
        qbUser.setFullName(userFullName);
        LoginType loginType = LoginType.valueOf(loginTypeRaw);
        return new AppSession(loginType, qbUser);
    }
}
