package main

import (
	"context"
	"fmt"
	"time"
)

const key = "key"

func main() {
	ctx, cancel := context.WithCancel(context.Background())

	valueCtx := context.WithValue(ctx, key, "Playing games")
	go playGame(valueCtx)
	time.Sleep(10 * time.Second)
	cancel()

	time.Sleep(5 * time.Second)
}

func playGame(ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			// get value
			fmt.Println(ctx.Value(key), "is cancel")
			return
		default:
			// get value
			fmt.Println(ctx.Value(key), "geming")
			time.Sleep(2 * time.Second)
		}
	}
}
