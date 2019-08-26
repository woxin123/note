package main

import "fmt"

func main() {
	var RMB = []int{200, 100, 20, 10, 5, 1}
	x := 1024
	count := 0
	for _, v := range RMB {
		use := int(x / v)
		x = x % v
		count += use
		if use == 0 {
			continue
		}
		fmt.Printf("需要使用 %d 的面额 %d 张，", v, use)
		fmt.Printf("剩余的金额为: %d。\n", x)
		if x == 0 {
			break
		}
	}
	fmt.Printf("共需要支付 %d 张.\n", count)
}
