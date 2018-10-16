package com.jetfiletransfer.mek.jetfiletransfer.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.jetfiletransfer.mek.jetfiletransfer.ProVersionActivity;
import com.jetfiletransfer.mek.jetfiletransfer.models.PurchasesModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class PurchasesManager implements PurchasesUpdatedListener {
    private BillingClient mBillingClient;
    private Activity context;

    public PurchasesManager(Activity context) {
        this.context = context;
        billingProcess();
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) { //satın alma işlemi bittikten sonra bu method otomatik çağırılır
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) { //satın alma başarılı
            for ( Purchase  purchase : purchases) {

                mBillingClient.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(int responseCode, String purchaseToken) {
                        if (responseCode == BillingClient.BillingResponse.OK) {
                            //satın alma tamamlandı yapacağınız işlemler
                            SharedPreferencesHelper secureSharedHelper = new SharedPreferencesHelper(context,true);
                            secureSharedHelper.userBuyProVersion();
                        }
                    }
                });
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {//kullanıcı iptal etti
            // Handle an error caused by a user canceling the purchase flow.
            billingCanceled(); //kullanıcı iptal etti

        } else {
            billingCanceled(); //bir sorun var
        }
    }
    private void billingCanceled() {
        //Kullanıcı iptal ettiğinde yapılacak işlemler
    }
    private void billingProcess(){
        mBillingClient = BillingClient.newBuilder(context).setListener(this).build(); //BillingClient objemizi oluşturduk

        mBillingClient.startConnection(new BillingClientStateListener() { //satın almaya hazır mı kontrolü
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // Satın almaya hazır
                    // BUTONLARI AKTIF ET
                    // enableOrDisableButtons(true); //butonları aktif et
                    rememberInAppBuy();
                } else {
                    //TODO Kullanıcıya uyarı ver
                    // Satın almaya hazır değil
                    Toast.makeText(context, "Ödeme sistemi için google play hesabını kontrol ediniz", Toast.LENGTH_SHORT).show();
                    // enableOrDisableButtons(false);//butonları pasif et
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Servise Bağlanamadı
                //TODO Kullanıcıya uyarı ver
                //  enableOrDisableButtons(false);//butonları pasif et
                Toast.makeText(context, "Ödeme sistemi şuanda geçerli değil", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void buyProVersion(String skuId) {
        //Bir defa satın almak için
        //Buradaki skuId , google playde tanımladığımız id'ler olmalı
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(skuId)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        mBillingClient.launchBillingFlow(context, flowParams);

    }
    public void rememberInAppBuy(){

        mBillingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(@BillingClient.BillingResponse int responseCode,
                                                          List<Purchase> purchasesList) {
                        if (responseCode == BillingClient.BillingResponse.OK
                                && purchasesList != null) {
                            for (Purchase purchase : purchasesList) {
                                //Kullanıcı satın almıs demektir
                                Log.d("al",purchase.getSku());
                                if(purchase.getSku().equals("buy_pro")){
                                    SharedPreferencesHelper secureSharedHelper = new SharedPreferencesHelper(context,true);
                                    secureSharedHelper.userBuyProVersion();
                                    EventBus.getDefault().post(new PurchasesModel(true));
                                    break;
                                }
                            }
                        }
                    }

                });
        EventBus.getDefault().post(new PurchasesModel(false));

    }
}
