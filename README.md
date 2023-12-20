# 예산 관리 어플리케이션

![save](https://github.com/mizuirohoshi7/budget-guard/assets/142835195/c9157a14-ce5b-41f8-b5ea-d4fa598544fa)

## 목차

1. [개발 기간](#개발-기간)
2. [기술 스택](#기술-스택)
3. [프로젝트 개요](#프로젝트-개요)
4. [프로젝트 일정관리](#프로젝트-일정관리)
5. [구현 기능 목록](#구현-기능-목록)
6. [설계 및 의도](#설계-및-의도)
7. [ERD](#erd)
8. [API 명세](#api-명세)
9. [테스트](#테스트)

## 개발 기간

2023-11-09 ~ 2023-11-13

## 기술 스택

<img src="https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="spring"/> <img src="https://img.shields.io/badge/spring data jpa-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="spring data jpa"/> <img src="https://img.shields.io/badge/querydsl-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="spring"/> <img src="https://img.shields.io/badge/spring security-6DB33F?style=for-the-badge&logo=springSecurity&logoColor=white" alt="spring security"/> <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="mysql"/>

## 프로젝트 개요

사용자들이 개인의 월별 예산을 식비, 교통비 등 카테고리 별로 설정하거나 추천받을 수 있습니다.

설정한 예산을 기반으로 카테고리 별 지출 가능액을 추천받거나 다른 사용자와의 통계를 내릴 수 있습니다.

위 기능들을 통해 자신의 한달 예산 및 지출을 관리하는데에 도움을 주는 어플리케이션입니다.

## 프로젝트 일정관리

**Git Projects 사용**

![image](https://github.com/mizuirohoshi7/budget-guard/assets/142835195/2f24fa80-3ef5-49ad-b232-a36eed2f5115)

[링크](https://github.com/users/mizuirohoshi7/projects/2/views/1)

## 구현 기능 목록

* 사용자
    * 회원가입
    * 로그인
    * JWT 기반 인가 및 인증

* 예산
    * 식비, 교통비, 여가비 별 월간 예산 설정
    * 예산 총액만 입력받아서 카테고리 별 예산 추천 받기

* 지출
    * 지출 CRUD
    * 설정한 월간 예산을 바탕으로 오늘 지출 가능한 금액을 추천 받기
    * 오늘 지출한 내용을 총액과 카테고리 별 금액으로 알림
    * 지난 달, 지난 요일, 다른 사용자 대비 소비율 통계

## 설계 및 의도

<details>
<summary>예산 총액만 입력받아서 카테고리 별 예산 추천 받기</summary>

```
카테고리 별 예산 설정에 어려움이 있는 사용자를 위해 예산 비율 추천 기능이 존재합니다.
예산 추천의 기준은 다른 사용자의 예산 비율의 평균입니다.
모든 사용자의 (카테고리 별 예산) / (예산 총액)을 평균내어 추천받을 사용자의 예산 총액에 곱한 값을 추천합니다.
단순히 카테고리 개수만큼 예산을 등분하기보다는 해당 방법이 더 합리적으로 예산을 분배할 수 있을 것이라 생각했습니다.
```
</details>

<details>
<summary>설정한 월간 예산을 바탕으로 오늘 지출 가능한 금액을 추천 받기</summary>

```
이번 달의 남은 예산과 남은 일수를 고려하여 오늘 지출 가능한 금액을 적절하게 추천합니다.
(남은 예산 / 남은 일수)를 잔여 예산 총액으로 설정하여, 카테고리 별 비율로 나누어서 추천합니다.
카테고리 별 비율은 처음 예산을 설정할 때의 (카테고리 별 예산) / (예산 총액)으로 계산합니다.
이 설계로 사용자는 전날에 과소비를 했다고 해도 오늘 한푼도 못쓰는 것이 아니라 적절한 지출을 할 수 있도록 도움 받습니다.
또한 예산 초과를 했더라도 미리 설정한 최소 지출 금액만큼은 추천받도록 구현했습니다.
```
</details>

<details>
<summary>사용자의 월간 오버뷰 테이블</summary>

```
사용자의 월간 총 예산과 월간 총 지출을 오버뷰 테이블을 만들어서 따로 관리했습니다.
위 두 수치는 필요할때마다 매번 계산하기보다는 아예 따로 관리하는 것이 더 효율적이라고 생각했습니다.
예산이나 지출의 비율을 구할 때 자주 필요하기 때문입니다.
이 어플리케이션은 월간 예산 및 지출을 관리해주는 것이기 때문에 매월 1일 0시 0분에 스케쥴러로 오버뷰 테이블을 초기화합니다.
```
</details>

## ERD

![image](https://github.com/mizuirohoshi7/budget-guard/assets/142835195/02741c8b-def1-40b3-ab2a-02fa47f6456b)

## API 명세

**Spring Rest Docs 기반 API 명세서**

[링크](https://mizuirohoshi7.github.io/budget-guard/)

![image](https://github.com/mizuirohoshi7/budget-guard/assets/142835195/db7a0775-79d2-413e-ae4c-8c4d3b630eb2)

## 테스트

### ✅ 24/24 (2초 33ms)

![image](https://github.com/wanted-preonboarding-team-m/02_geoRecommendEats/assets/57309311/c8265e01-9e0d-417f-865b-408e7e672322)

단위 테스트로 각 계층을 분리했습니다.
