WebSettings s = webView.getSettings();
s.setJavaScriptEnabled(true);
s.setDomStorageEnabled(true); // مینیو ڈیٹا سیو کرنے کے لیے
s.setDatabaseEnabled(true);
s.setAllowFileAccess(true);
s.setGeolocationEnabled(true); // لوکیشن پرمیشن پاپ اپ کے لیے

webView.setWebChromeClient(new WebChromeClient() {
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false); // لوکیشن ڈائریکٹ الاؤ کریں
    }
});
