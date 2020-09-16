SELECT
    wr._id AS id,
    wr.reference,
    wf.name AS workflow,
    wr.detail->>'suburb'::text AS suburb,
    wr.detail->>'town'::text AS town,
    wr.detail->>'school_name' AS school_name,
    wr.detail->>'school_emis' AS school_emis,
    con.company_name AS company_name,
    concat_ws(' ', wr.detail->>'name'::text, wr.detail->>'surname'::text) AS "name",
    wr.created_by,
    subp.value AS sub_programme,
    cow.value AS class_of_work,
    rcer.value AS root_cause_er,
    rr.value AS repair_required,
    pm.company_name AS project_manager,
    concat_ws(' ',pl.name, pl.surname) AS project_leader,
    arch.company_name AS architects,
    qs.company_name AS quantity_surveyor,
    se.company_name AS structural_engineer,
    ce.company_name AS civil_engineer,
    ee.company_name AS electrical_engineer,
    me.company_name AS mechanical_engineer,
    hsa.company_name AS health_safety_agent,
    jg.value AS job_grading,
    fw.value As further_works,
    bwi.value AS build_work_inspector,
    concat_ws(' ', ewi.name, ewi.surname) AS elec_work_inspector,
    concat_ws(' ', mwi.name, mwi.surname) AS mech_work_inspector,
    concat_ws(' ', pgm.name, pgm.surname) AS program_manager,
    concat_ws(' ', pl.name, pl.surname) AS project_lead,
    EXTRACT(EPOCH FROM wr.created_date)::bigint AS created_date,
    wr.updated_by,
    EXTRACT(EPOCH FROM wr.updated_date)::bigint AS updated_date
FROM
    work_request wr
        LEFT JOIN workflow wf ON wr.lookup_workflow_id = wf._id
        LEFT JOIN employee pgm ON (wr.detail->>'employee_2_id')::bigint = pgm._id
        LEFT JOIN employee pl ON (wr.detail->>'employee_3_id')::bigint = pl._id
        LEFT JOIN employee ewi ON (wr.detail->>'employee_5_id')::bigint = ewi._id
        LEFT JOIN employee mwi ON (wr.detail->>'employee_6_id')::bigint = mwi._id
        LEFT JOIN service_provider pm ON (wr.detail->>'service_provider_1_id')::bigint = pm._id
        LEFT JOIN service_provider arch ON (wr.detail->>'service_provider_2_id')::bigint = arch._id
        LEFT JOIN service_provider qs ON (wr.detail->>'service_provider_3_id')::bigint = qs._id
        LEFT JOIN service_provider se ON (wr.detail->>'service_provider_4_id')::bigint = se._id
        LEFT JOIN service_provider ce ON (wr.detail->>'service_provider_5_id')::bigint = ce._id
        LEFT JOIN service_provider ee ON (wr.detail->>'service_provider_6_id')::bigint = ee._id
        LEFT JOIN service_provider me ON (wr.detail->>'service_provider_7_id')::bigint = me._id
        LEFT JOIN service_provider hsa ON (wr.detail->>'service_provider_8_id')::bigint = hsa._id
        LEFT JOIN contractor con ON (wr.detail->>'contractor_1_id')::bigint = con._id
        LEFT JOIN lookup_value subp ON (wr.detail->>'lookup_sub_programme_id')::bigint=subp._id
        LEFT JOIN lookup_value jg ON (wr.detail->>'lookup_job_grading_id')::bigint=jg._id
        LEFT JOIN lookup_value fw ON (wr.detail->>'lookup_further_works_id')::bigint=fw._id
        LEFT JOIN asset_public.asset_link apal ON (wr.detail->>'school_emis')::text = apal.external_id
        LEFT JOIN asset_public.asset apa ON apal.asset_id=apa.asset_id
        , LATERAL (
        SELECT string_agg(cow.value, ',') AS value FROM
            jsonb_array_elements((wr.detail->>'lookup_class_of_work_id')::jsonb) AS cow_id
                LEFT JOIN
            lookup_value cow ON (cow_id::text)::bigint=cow._id
        ) AS cow,
    LATERAL (
        SELECT string_agg(rcer.value, ',') AS value FROM
            jsonb_array_elements((wr.detail->>'lookup_root_cause_of_er_id')::jsonb) AS rcer_id
                LEFT JOIN
            lookup_value rcer ON (rcer_id::text)::bigint=rcer._id
        ) AS rcer,
    LATERAL (
        SELECT string_agg(rr.value, ',') AS value FROM
            jsonb_array_elements((wr.detail->>'lookup_repair_required_id')::jsonb) AS rr_id
                LEFT JOIN
            lookup_value rr ON (rr_id::text)::bigint=rr._id
        ) AS rr,
    LATERAL (
        SELECT string_agg(concat_ws(' ', bwi.name, bwi.surname), ',') AS value FROM
            jsonb_array_elements((wr.detail->>'employee_4_id')::jsonb) AS bwi_id
                LEFT JOIN
            employee bwi ON (bwi_id::text)::bigint=bwi._id
        ) AS bwi
WHERE
    wr.deleted IS NULL