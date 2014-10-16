Distributed-Queue
=================

# Simple Distribute Queue like Hazelcast
라고 시작하였으나 현재 BlockingDequeSupport만 동작...ㅡㅡ;;

## Basic Concept
* SkipList를 활용하여 Tree의 balancing으로 인한 locking이 없는 map을 만들려고 하였음.
* SkipList와 마찬가지로 확률에 기반한 높이 증가로 tree를 구성함.
* 기존 SkipList와 차이점은 정렬된 상태를 leaf로 하는 것이 아니라 Queue를 leaf로 하여 map으로도 사용가능하고 queue로도 사용가능하도록 함 
* 용도는 Queue에 값을 넣고 뺄때 실제로 제거되지 않고 추후에 다른 액션을 통해 실제로 값을 큐에서 제거(2PC)하도록 하려고 map으로 해당 key의 데이터를 제거할 수 있도록 만듬

## Result
* 다른 tree 구현체보다 빠로게 동작할 것으로 생각했으나 linked list이기때문에 성능적으로 어떨지 모르겠음.
