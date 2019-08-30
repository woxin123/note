package main

import (
	"context"
	"fmt"
	"sync"
	"time"
)

var (
	wg sync.WaitGroup
)

func work(ctx context.Context) error {
	defer wg.Done()
	for i := 0; i < 1000; i++ {
		select {
		case <-time.After(2 * time.Second):
			fmt.Println("Doing some work ", i)
		case <-ctx.Done():
			fmt.Println("Cancel the context", i)
			fmt.Println(ctx.Err())
			return ctx.Err()
		}
	}
	return nil
}

func main() {
	ctx, cancel := context.WithTimeout(context.Background(), 4*time.Second)
	defer cancel()

	wg.Add(1)
	go work(ctx)
	wg.Wait()

	fmt.Println("Finished. I'am going home")
}
