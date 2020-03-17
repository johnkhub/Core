Querying and  Reporting API
============================

Reporting is done via materialized views. Typically these are based on other views.

|Schema|Materialised|View name                       |Description|
-------|------------|--------------------------------|------------
|dtpw  | Y          | dtpw_core_report_view          ||
|public| N          | asset_core_view                ||
|asset | N          | land_parcel_view               ||
|dtpw  | N          | asset_core_dtpw_view           ||
|dtpw  | N          | asset_core_dtpw_view_with_lpi  ||



REFRESH MATERIALIZED VIEW dtpw_core_report_view;