from influxdb_client import InfluxDBClient
from influxdb_client.client.write_api import ASYNCHRONOUS
import csv

bucket = "" # database
organization = ""
token = ""
client = InfluxDBClient(url="http://localhost:8086", token=token, org=organization)
query_api = client.query_api()

csv_result = query_api.query_csv('from(bucket: "dron") |> range(start: 2021-11-30T00:00:00Z, stop: 2021-11-30T23:59:59Z) |> filter(fn: (r) => r._measurement == "datos_dht" or r._measurement == "datos_telemetria" ) |> keep(columns: ["_field", "_time", "_value"]) |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")')
csv_file = open('output.csv', "w",newline='')
writer = csv.writer(csv_file)
for row in csv_result:
     writer.writerow(row)
csv_file.close()
