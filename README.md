실행방법

터미널 두개 따로 열기

\[생활 루틴 추천 ai 모델]

cd routine\_ai

python -m pip install -r requirements.txt 

python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload



\[dto]

POST http://127.0.0.1:8000/recommend



{

&nbsp; "user\_id": "5",

&nbsp; "top\_k": 10,

&nbsp; "exclude\_already\_planned": false,

&nbsp; "allow\_owned": true

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

&nbsp; "texts": \["다이소", "이마트", "노브랜드", "홈플러스"]

}





{

&nbsp;	"items":\["생필품", "생필품", "생필품", "생필품"]

}





