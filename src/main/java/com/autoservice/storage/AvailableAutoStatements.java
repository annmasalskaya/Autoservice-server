package com.autoservice.storage;

import AllAutos.*;
import com.autoservice.storage.mapping.AvailableAutoEntity;
import com.google.common.base.Preconditions;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.*;
import com.netflix.astyanax.query.PreparedCqlQuery;
import com.netflix.astyanax.serializers.ByteSerializer;
import com.netflix.astyanax.serializers.MapSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import org.apache.cassandra.db.marshal.UTF8Type;

import java.util.HashMap;
import java.util.Map;


public class AvailableAutoStatements extends TableStorage {

    private static class Holder {
        static final AvailableAutoStatements INSTANCE = new AvailableAutoStatements();
    }

    public static final AvailableAutoStatements getInstance() {
        return Holder.INSTANCE;
    }

    private ColumnFamily CF = ColumnFamily.newColumnFamily("availableauto", StringSerializer.get(),
            StringSerializer.get());

    private ColumnFamily<String, String> CF_AVAILABLEAUTO =
            new ColumnFamily<String, String>("availableauto",
                    StringSerializer.get(),
                    StringSerializer.get(),
                    ByteSerializer.get()

            );

    private static final String UPDATE_MODELS_AUCTION = " UPDATE availableauto SET modelscatalog= ?  WHERE carbrand= ?";

    private static final String UPDATE_AUTO = " UPDATE availableauto SET modelscatalog= ?  WHERE carbrand= ?";

    private static final String INSERT_AUTO = "\n" +
            "INSERT INTO availableauto (logo, carBrand, modelsAuction, modelsCatalog) VALUES " +
            "(?, ?, ?, ?)";

    private static final String SELECT_ALL = "SELECT * FROM availableauto";

    private PreparedCqlQuery<String, String> INSERT_AUTO_PREPARED
            = keyspace
            .prepareQuery(CF)
            .withCql(INSERT_AUTO)
            .asPreparedStatement();

    private PreparedCqlQuery<String, String> UPDATE_MODELS_AUCTION_PREPARED
            = keyspace
            .prepareQuery(CF)
            .withCql(UPDATE_MODELS_AUCTION)
            .asPreparedStatement();

    private PreparedCqlQuery<String, String> UPDATE_AUTO_PREPARED
            = keyspace
            .prepareQuery(CF)
            .withCql(UPDATE_AUTO)
            .asPreparedStatement();
    private PreparedCqlQuery<String, String> SELECT_AUTO_ALL_PREPARED
            = keyspace
            .prepareQuery(CF)
            .withCql(SELECT_ALL)
            .asPreparedStatement();


    public void insertAutoAuction(AvailableAutoEntity autoEntity) throws ConnectionException {
        Preconditions.checkNotNull(autoEntity.getKey());
        Preconditions.checkNotNull(autoEntity.getKey().getCarBrand());

        OperationResult<CqlResult<String, String>> res = INSERT_AUTO_PREPARED.
                withStringValue(autoEntity.getKey().getCarBrand()).
                withStringValue(autoEntity.getLogo()).
                withValue(new MapSerializer<String, String>(UTF8Type.instance, UTF8Type.instance).toByteBuffer((autoEntity.getModelsAuction()))).
                withValue(new MapSerializer<String, String>(UTF8Type.instance, UTF8Type.instance).toByteBuffer((autoEntity.getModelsAuction()))).
                execute();

    }

    public void editModelsAuction(String carBrand, Map<String, String> modelsAuction) throws ConnectionException {

        OperationResult<CqlResult<String, String>> res = UPDATE_MODELS_AUCTION_PREPARED.
                withStringValue(carBrand).
                withValue(new MapSerializer<String, String>(UTF8Type.instance, UTF8Type.instance).toByteBuffer(modelsAuction)).
                execute();

    }

    public void addModelsCatalog(String carBrand, Map<String, String> modelsCatalog) throws ConnectionException {

        OperationResult<CqlResult<String, String>> res = UPDATE_AUTO_PREPARED.
                withStringValue(carBrand).
                withValue(new MapSerializer<String, String>(UTF8Type.instance, UTF8Type.instance).toByteBuffer(modelsCatalog)).
                execute();

    }


    public ListAutos readAllAuction() throws ConnectionException {

        ListAutos listAutos = ListAutos$.MODULE$.apply();

        try {
            OperationResult<CqlResult<String, String>> result
                    = keyspace.prepareQuery(CF)
                    .withCql(SELECT_ALL)
                    .execute();
            String carBrand, logo, amount = null, list = null;

            for (Row<String, String> row : result.getResult().getRows()) {

                Map<String, String> modelsAuction = row.getColumns().getValue("modelsauction",
                        new MapSerializer<String, String>(UTF8Type.instance, UTF8Type.instance)
                        , new HashMap<String, String>());
                carBrand = row.getColumns().getColumnByName("carbrand").getStringValue();
                logo = row.getColumns().getColumnByName("logo").getStringValue();
                for (Map.Entry<String, String> entry : modelsAuction.entrySet()) {

                    if (entry.getKey().equals("amount"))
                        amount = entry.getValue();
                    else list = entry.getValue();

                }
                ModelsAuction models = ModelsAuction$.MODULE$.apply(amount, list);
                AvailableAuto availableAuto = AvailableAuto$.MODULE$.apply(carBrand, logo, models);
                listAutos.addAuto(availableAuto);

            }

        } catch (ConnectionException e) {
        }
        return listAutos;
    }

    public ListAutos readAllCatalog() throws ConnectionException {

        ListAutos listAutos = ListAutos$.MODULE$.apply();

        try {
            OperationResult<CqlResult<String, String>> result
                    = keyspace.prepareQuery(CF)
                    .withCql(SELECT_ALL)
                    .execute();
            String carBrand, logo, amount = null, list = null;

            for (Row<String, String> row : result.getResult().getRows()) {

                Map<String, String> modelsAuction = row.getColumns().getValue("modelscatalog",
                        new MapSerializer<String, String>(UTF8Type.instance, UTF8Type.instance)
                        , new HashMap<String, String>());
                carBrand = row.getColumns().getColumnByName("carbrand").getStringValue();
                logo = row.getColumns().getColumnByName("logo").getStringValue();
                for (Map.Entry<String, String> entry : modelsAuction.entrySet()) {

                    if (entry.getKey().equals("amount"))
                        amount = entry.getValue();
                    else list = entry.getValue();

                }
                ModelsAuction models = ModelsAuction$.MODULE$.apply(amount, list);
                AvailableAuto availableAuto = AvailableAuto$.MODULE$.apply(carBrand, logo, models);
                listAutos.addAuto(availableAuto);

            }

        } catch (ConnectionException e) {
        }
        return listAutos;
    }

    public ListAutos readByCarBrand(String carBrand) throws ConnectionException {

        ListAutos listAuto = ListAutos$.MODULE$.apply();
        int counter = 0;
        OperationResult<ColumnList<String>> res = keyspace.prepareQuery(CF_AVAILABLEAUTO)
                .getRow(carBrand)
                .execute();
        String logo = null, amount = null, list = null;
        for (Column<String> c : res.getResult()) {
            if (counter == 1)
                logo = c.getStringValue();
            else if (counter == 2) {
                amount = c.getStringValue();
            } else if (counter == 3) {
                list = c.getStringValue();
                ModelsAuction models = ModelsAuction$.MODULE$.apply(amount, list);
                AvailableAuto availableAuto = AvailableAuto$.MODULE$.apply(carBrand, logo, models);
                listAuto.addAuto(availableAuto);

            }

            counter++;

        }
        return listAuto;
    }

}
