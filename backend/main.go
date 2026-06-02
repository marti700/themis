package main

import (
	"bytes"
	"context"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"strconv"

	"github.com/gorilla/mux"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/marti700/themis/internal/database"
	"time"

	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	otelruntime "go.opentelemetry.io/contrib/instrumentation/runtime"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc"
	sdkmetric "go.opentelemetry.io/otel/sdk/metric"
	"go.opentelemetry.io/otel/sdk/resource"
	semconv "go.opentelemetry.io/otel/semconv/v1.21.0"
)

// initOtel wires up the OpenTelemetry metric pipeline:
//   OTLP gRPC exporter → MeterProvider → global otel.SetMeterProvider
//
// The exporter endpoint is read from OTEL_EXPORTER_OTLP_ENDPOINT (set to
// http://alloy:4317 in docker-compose). Returns a shutdown func to flush
// and close the exporter cleanly on exit.
func initOtel(ctx context.Context) (func(), error) {
	res, err := resource.New(ctx,
		resource.WithAttributes(semconv.ServiceName("themis-backend")),
	)
	if err != nil {
		return nil, err
	}

	exporter, err := otlpmetricgrpc.New(ctx, otlpmetricgrpc.WithInsecure())
	if err != nil {
		return nil, err
	}

	mp := sdkmetric.NewMeterProvider(
		sdkmetric.WithResource(res),
		sdkmetric.WithReader(sdkmetric.NewPeriodicReader(exporter)),
	)
	otel.SetMeterProvider(mp)

	if err := otelruntime.Start(otelruntime.WithMinimumReadMemStatsInterval(10 * time.Second)); err != nil {
		return nil, err
	}

	return func() {
		if err := mp.Shutdown(context.Background()); err != nil {
			log.Printf("Error shutting down meter provider: %v", err)
		}
	}, nil
}

func main() {
	ctx := context.Background()

	shutdown, err := initOtel(ctx)
	if err != nil {
		log.Fatal("Failed to initialize OTel: ", err)
	}
	defer shutdown()

	dsn := os.Getenv("DB_DSN")
	if dsn == "" {
		dsn = "postgres://themis:themis_dev@localhost:5432/themis?sslmode=disable"
	}
	conPool, err := pgxpool.New(ctx, dsn)
	if err != nil {
		log.Fatal("Bobo!! Error during db conection", err.Error())
	}

	queries := database.New(conPool)

	createCustomer := func(query *database.Queries) func(http.ResponseWriter, *http.Request) {
		return func(w http.ResponseWriter, r *http.Request) {
			if r.Method != http.MethodPost {
				log.Println("1")
				w.WriteHeader(http.StatusMethodNotAllowed)
				return
			}
			var params database.CreateCustomerParams
			json.NewDecoder(r.Body).Decode(&params)
			cus, err := query.CreateCustomer(r.Context(), &params)
			log.Printf("%v", cus)
			if err != nil {
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}

			var buf bytes.Buffer
			err = json.NewEncoder(&buf).Encode(cus)

			if err != nil {
				log.Printf("Error during parsing of database returned customer, %s", err.Error())
				http.Error(w, "Error parsing customer", http.StatusInternalServerError)
			}

			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusCreated)
			w.Write(buf.Bytes())
		}
	}

	getCustomer := func(queries *database.Queries) func(http.ResponseWriter, *http.Request) {
		return func(w http.ResponseWriter, r *http.Request) {
			if r.Method != http.MethodGet {
				w.WriteHeader(http.StatusMethodNotAllowed)
				return
			}

			cusId, err := strconv.Atoi(mux.Vars(r)["id"])
			if err != nil {
				http.Error(w, "Invalid id, it must be a valid number", http.StatusInternalServerError)
				return
			}

			cus, err := queries.GetCustomer(r.Context(), int32(cusId))
			if err != nil {
				log.Printf("Error getting user from database %s", err.Error())
			}

			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			if err := json.NewEncoder(w).Encode(&cus); err != nil {
				log.Printf("Bulto!! JSON decode error: %v", err)
				log.Printf("Bulto!! Here is what we are trying to parse: %v", cus)
				http.Error(w, "Check your JSON format: "+err.Error(), http.StatusInternalServerError)
				return
			}
		}
	}

	r := mux.NewRouter()
	r.HandleFunc("/users", createCustomer(queries))
	r.HandleFunc("/users/{id}", getCustomer(queries))

	// otelhttp.NewHandler wraps the entire router and automatically emits
	// http_server_request_duration_seconds for every request — count, duration,
	// and status code are all captured without touching individual handlers.
	err = http.ListenAndServe(":9094", otelhttp.NewHandler(r, "themis-backend"))
	if err != nil {
		log.Fatal(err.Error())
	}
}
