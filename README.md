실행방법

터미널 두개 따로 열기





\[생활 루틴 추천 ai 모델]

cd routine\_ai

python -m pip install -r requirements.txt

python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload



\[dto]

POST http://127.0.0.1:8000/recommend



{

  "user\_id": "5",

  "top\_k": 10,

  "exclude\_already\_planned": false,

  "allow\_owned": true

}



{

"items":\[

"카페자리잡기",

"텀블러세척",

"카페스터디모임",

"도시락/밀프렙포장",

"커피리필타임",

"물 마시기",

"감사일기3가지",

"근력운동상체",

"테이크아웃컵분리수거 배출",

"물2컵마시기"

]

}



\[카테고리 루틴 추천 ai 모델]

cd categori\_ai

python -m pip install -r requirements.txt

python -m uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload





\[dto]



{

  "texts": \["다이소", "이마트", "노브랜드", "홈플러스"]

}





{

 	"items":\["생필품", "생필품", "생필품", "생필품"]

}





\[생활 루틴 추천 ai 모델]

python -m pip install -r requirements.txt

python -m uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload





\[dto]



{

&nbsp; "user\_id": 12345,

&nbsp; "transactions": \[

&nbsp;   {"user\_id": 12345, "ts": "2024-11-01", "transactionType": "1", "transactionBalance": 1200000, "transactionAfterBalance": 2500000},

&nbsp;   {"user\_id": 12345, "ts": "2024-11-15", "transactionType": "2", "transactionBalance": 300000,  "transactionAfterBalance": 2200000},

&nbsp;   {"user\_id": 12345, "ts": "2024-12-01", "transactionType": "1", "transactionBalance": 1300000, "transactionAfterBalance": 2800000}

&nbsp; ],

&nbsp; "top\_k": 10

}



{

"user\_id": 12345,

"top\_k": 10,

"results":\[

{

"bankName": "웰컴저축은행",

"accountTypeName": "정기예금 (단리)",

"accountDscription": "여유자금을 일정기간 동안 확정금리로 예치하여 이자 수익을 얻을 수 있는 거치식 상품",

"subscriptionPeriod": 0,

"interesRate": 0.5,

"score": 2.221612130109189,

"rank": 1

}

]

}









