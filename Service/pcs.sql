SELECT ast.asset_id, ast.asset_type_code, ast.code, ast.name, ast.func_loc_path, latitude, longitude
     , rd.k districtcode
     , rd.v districtvalue
     , rm.k municipalitycode
     , rm.v municipalityvalue
     , rt.k towncode
     , rt.v townvalue
FROM public.asset ast
         left join public.location on location.asset_id = ast.asset_id
         left join asset.a_tp_envelope ate on ate.asset_id = ast.asset_id
         left join asset.ref_district rd on rd.k = ate.district_code
         left join asset.ref_municipality rm on rm.k = ate.municipality_code
         left join asset.ref_town rt on rt.k = ate.town_code
WHERE
    (asset_type_code = 'ENVELOPE')


    private String mainAssetQuery(String ltreefilter, String boundaryQueryFields, String boundaryJoinStatements, String whereClauses) {
    final String s = "SELECT ast.asset_id, ast.asset_type_code, ast.code, ast.name, ast.func_loc_path, al.external_id, latitude, longitude\n" +
            boundaryQueryFields + "\n" +
            "FROM public.asset ast\n" +
            "left join public.location on location.asset_id = ast.asset_id\n" +
            "left join asset_link al on al.asset_id = ast.asset_id \n" +
            "left join external_id_type eit on eit.type_id = al.external_id_type and eit.name = 'EMIS'\n" +
            boundaryJoinStatements + "\n" +
            "WHERE\n" +
            whereClauses + "\n" +
            ltreefilter + "\n" +
            "ORDER BY asset_type_code, code";
log.debug("Generated Asset Query:\n" + s);
return s;
}

As dit sal help kan jy ook in die PCS code kyk: AssetCoreRepositoryImpl.java