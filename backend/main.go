package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/gorilla/mux"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/marti700/themis/internal/customer"
	"github.com/marti700/themis/internal/database"

	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	otelruntime "go.opentelemetry.io/contrib/instrumentation/runtime"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc"
	sdkmetric "go.opentelemetry.io/otel/sdk/metric"
	"go.opentelemetry.io/otel/sdk/resource"
	semconv "go.opentelemetry.io/otel/semconv/v1.21.0"
)

// initOtel wires up the OpenTelemetry metric pipeline:
//
//	OTLP gRPC exporter → MeterProvider → global otel.SetMeterProvider
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
	customerHandler := &customer.Handler{Queries: queries}

	r := mux.NewRouter()
	r.HandleFunc("/users", customerHandler.Create).Methods(http.MethodPost)
	r.HandleFunc("/users/{id}", customerHandler.Get).Methods(http.MethodGet)

	// otelhttp.NewHandler wraps the entire router and automatically emits
	// http_server_request_duration_seconds for every request — count, duration,
	// and status code are all captured without touching individual handlers.
	err = http.ListenAndServe(":9094", otelhttp.NewHandler(r, "themis-backend"))
	if err != nil {
		log.Fatal(err.Error())
	}
}
