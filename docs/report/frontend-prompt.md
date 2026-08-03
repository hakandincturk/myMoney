# Frontend Agent Prompt — Reports (Rapor) Ekranı

> Backend Report modülü (revizyon 2) tamamlandı. Aşağıdaki "PROMPT BAŞLANGICI" / "PROMPT SONU"
> arasını olduğu gibi kopyalayıp myMoney-frontend agent'ına ver.

---

## PROMPT BAŞLANGICI

myMoney frontend projesine **Reports (Rapor)** ekranını ekle. Backend tarafı tamamlandı ve 5
endpoint canlı. Sözleşme, mimari kurallar ve gerçek örnek response'lar aşağıda tam olarak
verilmiştir — başka bir kaynağa bakmana gerek yok, backend'e soru sorma.

Ekran tasarımı, bileşen ağacı ve scaffold sırası için repo içindeki `docs/pages/Reports.md`
dokümanını kullan. Mimari kurallar için `.claude/skills/mymoney-frontend/SKILL.md` geçerlidir;
kritik olanları aşağıda ayrıca yazdım, bunlar pazarlık konusu değil.

---

## 0. Uyulacak mimari kurallar

Bunlar ihlal edilirse çıkan kod reddedilir.

1. **DTO'lar namespace pattern ile yazılır.** Düz `interface` değil:
   ```ts
   // src/types/report.ts
   export namespace ReportDTOs {
     export type PeriodKind = 'ACTUAL' | 'PARTIAL' | 'PROJECTED'
     // ...
   }
   ```
   Ardından `src/types/index.ts` içine `export * from './report'` eklenir.

2. **Endpoint URL'leri asla string literal olarak yazılmaz.** Hepsi `src/config/ApiUrl.ts`
   enum'ına `// Report endpoints` yorum bloğuyla eklenir ve kullanımda enum'dan okunur.

3. **Tüm istekler RTK Query üzerinden yapılır.** `fetch`, `axios` veya `useEffect` + local state
   yasak. `src/services/reportApi.ts`:
   - `createApi`, `reducerPath: 'reportApi'`, `baseQuery: baseQueryWithReauth`
   - `tagTypes: ['Report']`, hepsi `build.query` (mutation yok)
   - `providesTags` dönem bazlı, örn. `{ type: 'Report', id: 'SUMMARY_2026_8' }`
   - `keepUnusedDataFor: CACHE_CONFIG.isEnabled() ? CACHE_CONFIG.DURATIONS.DETAIL : 0`

4. **`src/store/store.ts`'ye 4 noktadan entegrasyon zorunlu** — biri atlanırsa hook'lar runtime'da
   boş döner: `rootReducer`, `persistConfig.whitelist` (cache-enabled dalı), `middleware.concat`,
   `serializableCheck.ignoredPaths`.

5. **`any` yasak.** `unknown` + narrowing kullan.

6. **Tüm response'lar `CommonTypes.ApiResponse<T>` ile sarılı.** Hook dönüşleri
   `ApiResponse<ReportDTOs.SummaryResponse>` şeklinde tiplenir, veriye `data.data` ile erişilir.

7. **Kullanıcıya görünen her metin i18next'ten gelir.** Yeni `reports.*` namespace'i
   `src/i18n/locales/tr.json` **ve** `en.json`'a **aynı anahtar yapısıyla** eklenir; iki dosyanın
   anahtar kümesi birebir eşit olmalı. Ay adları için mevcut `months.*` anahtarları kullanılır.

8. **Tailwind class'ları çift modlu yazılır.** `bg-white dark:bg-mm-card`,
   `text-slate-900 dark:text-mm-text` gibi. Özel hex literal yok, `mm-*` paleti kullanılır.

9. **Route `lazy()` + `Suspense` + `ProtectedRoute` ile eklenir.** Path: `/reports`. Sidebar'a
   `AppLayout.tsx` içine `faChartPie` ikonuyla `t('sidebar.reports')` entry'si eklenir.
   (v2 dashboard'daki CommandBar'ın "Raporlar" aksiyonu zaten `/reports`'a yönlendiriyor ama route
   yok — bu ekran onu da kapatacak.)

10. **Bileşen yapısını yeniden tasarlama.** `docs/pages/Reports.md` §6'daki ağaç geçerli:
    `ReportPeriodBar`, `PeriodComparisonHero`, `ReportKpiStrip`, `ReportTimelineChart` (lazy),
    `TagBreakdownPanel`, `TagDonutPanel` (lazy), `TopExpensesPanel`, `RecurringExpensesPanel`,
    `Sparkline`, `DeltaBadge`, `ReportInsightBar`.

11. **Formatlama helper'ları `@/utils/formatters`'tan gelir** (`formatTRY`, `formatCompactTRY`,
    `formatPercent`, `formatShortDate`, `daysUntil`). Yenisini yazma.

12. **`console.log` bırakma, kod yorumlarını İngilizce yaz.**

---

## 1. Genel sözleşme

Tüm uçlar `GET`, hepsi JWT ile korunuyor, hiçbiri `userId` parametresi almıyor — veri oturumdaki
kullanıcıya scope'lu.

```
Authorization: Bearer <token>
```

Tüm response'lar `ApiResponse<T>` ile sarılı:

```ts
interface ApiResponse<T> {
  type: boolean       // true = başarılı
  message: string     // Türkçe kullanıcı mesajı
  timestamp: string   // "2026-08-03T16:51:27.740238" — her zaman ISO string, timezone yok
  data: T
}
```

**Hata davranışı:**
- `400` — `year` 2000-2100 dışında veya `month` 1-12 dışında. Alan bazlı validasyon hatasında
  `data` bir `Record<string, string[]>` (alan adı → hata mesajları).
- `401` — token yok, geçersiz veya süresi dolmuş. Body yine `ApiResponse` şeklinde,
  `timestamp` **string**. (Eskiden sayı dizisi dönüyordu, düzeltildi — özel parse yazma.)
- **404 asla dönmez.** Kullanıcının hiç verisi yoksa `200` + sıfırlı/boş yapı gelir.
- `limit`, `pastMonths`, `futureMonths`, `lookbackMonths`, `minOccurrence` negatif gelirse hata
  dönmez, default'a düşer; üst sınırı aşan değer sınıra çekilir.

**Para alanları** JSON `number`, 2 ondalıklı (`12000.00`). **Tarihler** `"YYYY-MM-DD"` string.
**Oranlar** `number`, 2 ondalıklı; bazıları `null` olabilir (aşağıda işaretli).

---

## 2. `src/types/report.ts`

```ts
export namespace ReportDTOs {
  export type PeriodKind = 'ACTUAL' | 'PARTIAL' | 'PROJECTED'
  export type FlowType = 'EXPENSE' | 'INCOME'
  export type TagSumMode = 'DISTRIBUTED' | 'DOUBLE_COUNT'
  export type RecurringKind = 'INSTALLMENT' | 'REPEATED'

  /** One month of totals. income = realized + pending, net = income - expense. */
  export type PeriodTotals = {
    year: number
    month: number              // 1-12
    label: string              // "JANUARY".."DECEMBER" -> i18n key months.*
    kind: PeriodKind
    income: number             // TRY
    expense: number            // TRY
    net: number                // TRY, can be negative
    realizedIncome: number     // paid
    pendingIncome: number      // planned, not paid
    realizedExpense: number
    pendingExpense: number
    transactionCount: number
    installmentCount: number
  }

  /** Transition between two periods, always read forward in time. */
  export type PeriodDelta = {
    incomeChangeRate: number | null    // null when the denominator period is 0 -> render "—"
    expenseChangeRate: number | null   // null when the denominator period is 0 -> render "—"
    netChangeAmount: number            // TRY, signed
  }

  export type BusiestDay = { date: string; amount: number }

  export type TopTag = {
    tagId: number | null       // null => untagged group won
    name: string               // "UNTAGGED" when tagId is null
    amount: number
    share: number              // % of the month's expense
  }

  export type TagRef = { id: number; name: string }

  export type PeriodRef = { year: number; month: number; label: string }

  // GET /report/summary
  export type SummaryResponse = {
    previous: PeriodTotals             // never null; zero filled when there is no data
    current: PeriodTotals
    next: PeriodTotals                 // never null
    deltaVsPrevious: PeriodDelta       // previous -> current, denominator = previous
    deltaVsNext: PeriodDelta           // current -> next, denominator = current
    savingRate: number                 // %, can be negative, not clamped
    averageDailyExpense: number        // expense / daysElapsed
    daysInMonth: number                // 28..31
    daysElapsed: number                // divisor used above; PARTIAL month -> days so far
    projectedMonthEndExpense: number   // PARTIAL: naive linear estimate, otherwise = expense
    busiestDay: BusiestDay | null      // null when the month has no expense
    topTag: TopTag | null              // null when the month has no expense
  }

  // GET /report/timeline
  export type TimelinePoint = PeriodTotals & {
    /** Backward looking: debt actually carried at the end of that month. Flat in the future. */
    cumulativeRemainingDebt: number
    /** Forward looking: unpaid installments due after that month. Monotonically decreasing. */
    projectedRemainingDebt: number
  }
  export type TimelineResponse = { points: TimelinePoint[] }

  // GET /report/tag-breakdown
  export type TagBreakdownItem = {
    tagId: number | null       // null => untagged group
    name: string               // "UNTAGGED" when tagId is null
    amount: number
    percentage: number         // % of total
    previousAmount: number     // 0 when the tag did not exist last month
    changeRate: number | null  // null when previousAmount is 0 -> render "—"
    transactionCount: number
  }
  export type TagBreakdownResponse = {
    items: TagBreakdownItem[]  // desc by amount, capped by `limit`
    total: number              // NOT affected by `limit`
    previousTotal: number      // NOT affected by `limit`
    untaggedAmount: number     // NOT affected by `limit`
  }

  // GET /report/top-expenses
  export type TopExpenseItem = {
    transactionId: number
    installmentId: number
    name: string
    description: string | null
    amount: number             // the installment's share of THIS month
    date: string
    accountName: string
    contactName: string | null
    tags: TagRef[]             // can be empty
    installmentNumber: number
    totalInstallment: number
    paid: boolean
  }
  export type TopExpensesResponse = {
    items: TopExpenseItem[]
    periodTotal: number        // month total for the selected flow type
  }

  // GET /report/recurring
  export type RecurringMonthAmount = { year: number; month: number; amount: number }
  export type RecurringItem = {
    groupKey: string           // "TX:{id}" | "NAME:{normalizedName}" — use as React key
    name: string
    kind: RecurringKind
    occurrenceCount: number
    monthsSpan: number
    averageAmount: number      // totalAmount / monthsSpan (monthly average)
    totalAmount: number
    lastAmount: number
    lastDate: string
    nextExpectedDate: string | null  // INSTALLMENT: real due date, REPEATED: estimate
    amountByMonth: RecurringMonthAmount[]  // every month of the window, gaps filled with 0
    tags: TagRef[]
    active: boolean
  }
  export type RecurringResponse = {
    items: RecurringItem[]              // desc by averageAmount, capped by `limit`
    monthlyFixedCost: number            // NOT affected by `limit` — sum over all groups
    fixedCostShareOfIncome: number      // %, 0 when the current month income is 0
    windowStart: PeriodRef              // always present, even when items is empty
    windowEnd: PeriodRef                // always the server's current month
  }
}
```

---

## 3. Endpoint'ler

| Endpoint | Parametre | Zorunlu | Default | Sınır |
|---|---|---|---|---|
| `GET /report/summary` | `year` | ✔ | — | 2000-2100 |
| | `month` | ✔ | — | 1-12 |
| `GET /report/timeline` | `year`, `month` | ✔ | — | aynı |
| | `pastMonths` | ✖ | 6 | 0-24 |
| | `futureMonths` | ✖ | 6 | 0-24 |
| `GET /report/tag-breakdown` | `year`, `month` | ✔ | — | aynı |
| | `type` | ✖ | `EXPENSE` | `EXPENSE` \| `INCOME` |
| | `sumMode` | ✖ | `DISTRIBUTED` | `DISTRIBUTED` \| `DOUBLE_COUNT` |
| | `limit` | ✖ | 10 | 0-100 |
| `GET /report/top-expenses` | `year`, `month` | ✔ | — | aynı |
| | `type` | ✖ | `EXPENSE` | `EXPENSE` \| `INCOME` |
| | `limit` | ✖ | 5 | 0-50 |
| `GET /report/recurring` | `lookbackMonths` | ✖ | 6 | 1-24 |
| | `minOccurrence` | ✖ | 3 | 1-24 |
| | `limit` | ✖ | 10 | 0-100 |

**`/report/recurring` `year`/`month` almaz** — her zaman sunucunun bugününden geriye bakar, içinde
bulunulan ay dahil toplam `lookbackMonths` ay. Hangi aralığa baktığını `windowStart`/`windowEnd`
ile bildirir; panel başlığında bunu göster.

`timeline` nokta sayısı her zaman `pastMonths + 1 + futureMonths`.

Dört ay-bazlı uç aynı anda paralel çağrılabilir. Her biri için ayrı loading/error state kur ki biri
patlarsa diğerleri render edilebilsin.

---

## 4. Alan semantikleri ve UI kuralları

**`label`** — İngilizce büyük harf ay adı, i18n anahtarıdır. `t('months.' + point.label)` ile
çevir, ham gösterme.

**`kind`** — dönemin kesinlik seviyesi:
- `ACTUAL` (geçmiş ay) → normal, kesinleşmiş
- `PARTIAL` (içinde bulunulan ay) → "devam ediyor" vurgusu
- `PROJECTED` (gelecek ay) → grafikte kesikli çizgi + soluk dolgu, kartlarda rozet.
  **Yalnızca renge güvenme**, metin/desen de kullan.

**`realized` vs `pending`** — `realized` ödenmiş, `pending` planlanmış ama ödenmemiş. Stacked bar'da
alt segment realized, üst segment pending.

**`deltaVsPrevious` ve `deltaVsNext` ikisi de zaman yönünde ileri okunur.** Sözleşmenin en kolay
yanlış anlaşılan yeri burası:

```
deltaVsPrevious = previous -> current geçişi,  payda = previous
deltaVsNext     = current  -> next    geçişi,  payda = current
```

Örnek (aşağıdaki gerçek response'tan): `current.income = 51000`, `next.income = 48500` →
`deltaVsNext.incomeChangeRate = (48500 - 51000) / 51000 * 100 = -4.90`.
Yani **"gelecek ay gelirin %4.90 düşecek"**. `deltaVsNext.netChangeAmount = 14329.00` ise
"gelecek ay net %... değil, net **14.329 TL artacak**" demektir. İkisi de aynı yönde okunduğu için
tek bir `DeltaBadge` bileşeni her ikisinde de kullanılabilir.

**`changeRate === null`** → `"—"` göster. `?? 0` yapma, ortalamaya/toplama katma, %0 gösterme.

**`savingRate`** — negatif olabilir (gider > gelir). Backend clamp etmiyor; görsel olarak
clamp'lersen ham değeri tooltip'te göster.

**`averageDailyExpense` + `daysElapsed` + `daysInMonth` + `projectedMonthEndExpense`** birlikte
kullanılır:
- `kind === 'PARTIAL'` ise etiket **"bugüne kadar günlük ort."** olmalı (`daysElapsed` gün üzerinden),
  ve yanında `projectedMonthEndExpense` "ay sonu tahmini" olarak gösterilmeli.
- Diğer aylarda `daysElapsed === daysInMonth` ve `projectedMonthEndExpense === expense`; etiket
  sade "günlük ort." olur, tahmin gösterilmez.
- `projectedMonthEndExpense` **naif doğrusal tahmindir** (günlük ortalama × ayın gün sayısı). Ayın
  başında çok yüksek çıkabilir — "tahmin" olduğunu UI'da açıkça belirt.

**İki kalan borç metriği** — karıştırma:
- `cumulativeRemainingDebt`: geçmişe bakar, o ayın sonunda fiilen taşınan borç. Gelecek aylarda düz
  gider (gelecekte hiçbir taksit henüz ödenmemiştir).
- `projectedRemainingDebt`: geleceğe bakar, o ayın sonundan sonraki vadeye sahip ödenmemiş
  taksitlerin toplamı. Monoton azalır — **"plana uyarsan borcun nasıl erir" eğrisi budur.**

  Grafikte **`projectedRemainingDebt`** çiz (ikincil eksende, line). `cumulativeRemainingDebt`'i
  tooltip'te ek bilgi olarak göster. İkisini aynı seride birleştirme.

**`sumMode`:**
- `DISTRIBUTED` (default): bir işlem N etikete sahipse tutar N'e bölünür, kuruş artığı dahil →
  `Σ items.amount === total`. **Pasta/donut grafik için bunu kullan.**
- `DOUBLE_COUNT`: her etikete tam tutar yazılır → toplamlar `total`'ı aşar, `percentage` toplamı
  %100'ü geçer. Pasta grafikte kullanma, sadece liste görünümünde anlamlı.

**`total` / `previousTotal` / `untaggedAmount` / `monthlyFixedCost` `limit`'ten etkilenmez.**
Bunları `items` üzerinden yeniden hesaplama.

**`tagId === null`** → `name` sabit `"UNTAGGED"`, `t('reports.tags.untagged')` ile çevrilir.
Aynı durum `topTag` için de geçerli: **`topTag.tagId` null olabilir**, o zaman etiketsiz grup ayın
en büyüğü demektir.

**`top-expenses`** — sıralama **taksit satırı bazında** (işlemin toplam tutarına göre değil).
`3/12` rozetini yalnız `totalInstallment > 1` iken göster; tek çekimde de `installmentNumber: 1`,
`totalInstallment: 1` gelir. Kalemin payı = `amount / periodTotal`.

**`recurring`:**
- `kind: 'INSTALLMENT'` → çok taksitli tek işlem (`groupKey = "TX:{transactionId}"`); satır
  tıklanınca işlem detayına gidilebilir.
- `kind: 'REPEATED'` → aynı isimle tekrar eden ayrı işlemler (`groupKey = "NAME:{normalized}"`);
  tek bir işlem detayı yoktur, grup görünümü göster.
- `nextExpectedDate`: `INSTALLMENT` için gerçek taksit tarihi, `REPEATED` için **tahmin** — UI'da
  tahmin olduğunu belli et (`~` ön eki veya tooltip).
- `amountByMonth` deliksizdir, `Sparkline`'a doğrudan verilebilir.
- `active: false` → soluk göster, listeden çıkarma.
- `groupKey`'i React `key` olarak kullan (kayıtların `id`'si yok).
- Panel başlığında `windowStart`–`windowEnd` aralığını yaz
  (`t('months.' + windowStart.label)` + yıl).

---

## 5. Gerçek örnek response'lar

Aşağıdakiler backend'den gerçek HTTP çağrılarıyla alınmıştır (referans ay: 2026-08, "bugün"
2026-08-03).

### `GET /report/summary?year=2026&month=8`
```json
{"type":true,"message":"Rapor özeti getirildi","timestamp":"2026-08-03T16:51:27.740238","data":{
 "previous":{"year":2026,"month":7,"label":"JULY","kind":"ACTUAL","income":45000.00,"expense":23499.00,"net":21501.00,"realizedIncome":45000.00,"pendingIncome":0.00,"realizedExpense":23499.00,"pendingExpense":0.00,"transactionCount":5,"installmentCount":5},
 "current":{"year":2026,"month":8,"label":"AUGUST","kind":"PARTIAL","income":51000.00,"expense":28829.00,"net":22171.00,"realizedIncome":48500.00,"pendingIncome":2500.00,"realizedExpense":11600.00,"pendingExpense":17229.00,"transactionCount":7,"installmentCount":7},
 "next":{"year":2026,"month":9,"label":"SEPTEMBER","kind":"PROJECTED","income":48500.00,"expense":12000.00,"net":36500.00,"realizedIncome":0.00,"pendingIncome":48500.00,"realizedExpense":0.00,"pendingExpense":12000.00,"transactionCount":2,"installmentCount":2},
 "deltaVsPrevious":{"incomeChangeRate":13.33,"expenseChangeRate":22.68,"netChangeAmount":670.00},
 "deltaVsNext":{"incomeChangeRate":-4.9,"expenseChangeRate":-58.38,"netChangeAmount":14329.00},
 "savingRate":43.47,"averageDailyExpense":9609.67,"daysInMonth":31,"daysElapsed":3,
 "projectedMonthEndExpense":297899.77,
 "busiestDay":{"date":"2026-08-15","amount":12000.00},
 "topTag":{"tagId":2,"name":"Ev","amount":12000.00,"share":41.62}}}
```
Not: `projectedMonthEndExpense` ayın 3'ünde alınmış naif tahmindir; bu yüzden bu kadar yüksek.
UI'da mutlaka "tahmin" olarak etiketle.

### `GET /report/timeline?year=2026&month=8&pastMonths=2&futureMonths=2`
```json
{"type":true,"message":"Rapor zaman serisi getirildi","timestamp":"2026-08-03T16:51:27.783975","data":{"points":[
 {"year":2026,"month":6,"label":"JUNE","kind":"ACTUAL","income":0.00,"expense":5229.00,"net":-5229.00,"realizedIncome":0.00,"pendingIncome":0.00,"realizedExpense":5229.00,"pendingExpense":0.00,"transactionCount":2,"installmentCount":2,"cumulativeRemainingDebt":64328.00,"projectedRemainingDebt":29229.00},
 {"year":2026,"month":7,"label":"JULY","kind":"ACTUAL","income":45000.00,"expense":23499.00,"net":21501.00,"realizedIncome":45000.00,"pendingIncome":0.00,"realizedExpense":23499.00,"pendingExpense":0.00,"transactionCount":5,"installmentCount":5,"cumulativeRemainingDebt":40829.00,"projectedRemainingDebt":29229.00},
 {"year":2026,"month":8,"label":"AUGUST","kind":"PARTIAL","income":51000.00,"expense":28829.00,"net":22171.00,"realizedIncome":48500.00,"pendingIncome":2500.00,"realizedExpense":11600.00,"pendingExpense":17229.00,"transactionCount":7,"installmentCount":7,"cumulativeRemainingDebt":29229.00,"projectedRemainingDebt":12000.00},
 {"year":2026,"month":9,"label":"SEPTEMBER","kind":"PROJECTED","income":48500.00,"expense":12000.00,"net":36500.00,"realizedIncome":0.00,"pendingIncome":48500.00,"realizedExpense":0.00,"pendingExpense":12000.00,"transactionCount":2,"installmentCount":2,"cumulativeRemainingDebt":29229.00,"projectedRemainingDebt":0.00},
 {"year":2026,"month":10,"label":"OCTOBER","kind":"PROJECTED","income":0.00,"expense":0.00,"net":0.00,"realizedIncome":0.00,"pendingIncome":0.00,"realizedExpense":0.00,"pendingExpense":0.00,"transactionCount":0,"installmentCount":0,"cumulativeRemainingDebt":29229.00,"projectedRemainingDebt":0.00}]}}
```
OCTOBER veri olmayan aydır ve sıfırlı gelir — atlanmaz.

### `GET /report/tag-breakdown?year=2026&month=8`
```json
{"type":true,"message":"Etiket bazlı kırılım getirildi","timestamp":"2026-08-03T16:51:27.803123","data":{
 "items":[
  {"tagId":2,"name":"Ev","amount":12000.00,"percentage":41.62,"previousAmount":12000.00,"changeRate":0.0,"transactionCount":1},
  {"tagId":1,"name":"Market","amount":8400.00,"percentage":29.14,"previousAmount":6270.00,"changeRate":33.97,"transactionCount":1},
  {"tagId":null,"name":"UNTAGGED","amount":8200.00,"percentage":28.44,"previousAmount":5000.00,"changeRate":64.0,"transactionCount":2},
  {"tagId":3,"name":"Abonelik","amount":229.00,"percentage":0.79,"previousAmount":229.00,"changeRate":0.0,"transactionCount":1}],
 "total":28829.00,"previousTotal":23499.00,"untaggedAmount":8200.00}}
```

### `GET /report/top-expenses?year=2026&month=8`
```json
{"type":true,"message":"Ayın en büyük kalemleri getirildi","timestamp":"2026-08-03T16:51:27.834962","data":{
 "items":[
  {"transactionId":5,"installmentId":6,"name":"Buzdolabı","description":"Buzdolabı açıklaması","amount":12000.00,"date":"2026-08-15","accountName":"Garanti Bonus","contactName":null,"tags":[{"id":2,"name":"Ev"}],"installmentNumber":3,"totalInstallment":12,"paid":false},
  {"transactionId":7,"installmentId":9,"name":"Market Alışverişi","description":"Market Alışverişi açıklaması","amount":8400.00,"date":"2026-08-10","accountName":"Garanti Bonus","contactName":null,"tags":[{"id":1,"name":"Market"}],"installmentNumber":1,"totalInstallment":1,"paid":true},
  {"transactionId":9,"installmentId":14,"name":"İhtiyaç Kredisi","description":"İhtiyaç Kredisi açıklaması","amount":5000.00,"date":"2026-08-05","accountName":"Garanti Bonus","contactName":null,"tags":[],"installmentNumber":4,"totalInstallment":12,"paid":false},
  {"transactionId":8,"installmentId":10,"name":"Yakıt","description":"Yakıt açıklaması","amount":3200.00,"date":"2026-08-12","accountName":"Garanti Bonus","contactName":null,"tags":[],"installmentNumber":1,"totalInstallment":1,"paid":true},
  {"transactionId":13,"installmentId":18,"name":"Netflix","description":"Netflix açıklaması","amount":229.00,"date":"2026-08-05","accountName":"Garanti Bonus","contactName":null,"tags":[{"id":3,"name":"Abonelik"}],"installmentNumber":1,"totalInstallment":1,"paid":false}],
 "periodTotal":28829.00}}
```

### `GET /report/recurring`
```json
{"type":true,"message":"Tekrar eden harcamalar getirildi","timestamp":"2026-08-03T16:51:27.868852","data":{
 "items":[
  {"groupKey":"TX:5","name":"Buzdolabı","kind":"INSTALLMENT","occurrenceCount":2,"monthsSpan":2,"averageAmount":12000.00,"totalAmount":24000.00,"lastAmount":12000.00,"lastDate":"2026-08-15","nextExpectedDate":"2026-08-15","amountByMonth":[{"year":2026,"month":3,"amount":0.00},{"year":2026,"month":4,"amount":0.00},{"year":2026,"month":5,"amount":0.00},{"year":2026,"month":6,"amount":0.00},{"year":2026,"month":7,"amount":12000.00},{"year":2026,"month":8,"amount":12000.00}],"tags":[{"id":2,"name":"Ev"}],"active":true},
  {"groupKey":"TX:9","name":"İhtiyaç Kredisi","kind":"INSTALLMENT","occurrenceCount":4,"monthsSpan":4,"averageAmount":5000.00,"totalAmount":20000.00,"lastAmount":5000.00,"lastDate":"2026-08-05","nextExpectedDate":"2026-08-05","amountByMonth":[{"year":2026,"month":3,"amount":0.00},{"year":2026,"month":4,"amount":0.00},{"year":2026,"month":5,"amount":5000.00},{"year":2026,"month":6,"amount":5000.00},{"year":2026,"month":7,"amount":5000.00},{"year":2026,"month":8,"amount":5000.00}],"tags":[],"active":true},
  {"groupKey":"NAME:netflix","name":"Netflix","kind":"REPEATED","occurrenceCount":4,"monthsSpan":4,"averageAmount":229.00,"totalAmount":916.00,"lastAmount":229.00,"lastDate":"2026-08-05","nextExpectedDate":"2026-09-04","amountByMonth":[{"year":2026,"month":3,"amount":0.00},{"year":2026,"month":4,"amount":0.00},{"year":2026,"month":5,"amount":229.00},{"year":2026,"month":6,"amount":229.00},{"year":2026,"month":7,"amount":229.00},{"year":2026,"month":8,"amount":229.00}],"tags":[{"id":3,"name":"Abonelik"}],"active":true}],
 "monthlyFixedCost":17229.00,"fixedCostShareOfIncome":33.78,
 "windowStart":{"year":2026,"month":3,"label":"MARCH"},
 "windowEnd":{"year":2026,"month":8,"label":"AUGUST"}}}
```

---

## 6. Frontend'in kendi türeteceği değerler

Bunlar için **yeni endpoint yok**, client-side hesaplanacak:

- **"Sonraki ay" rozeti** doğrudan `deltaVsNext`'ten okunur; ham `PeriodTotals`'tan tekrar
  hesaplama.
- **`ReportInsightBar` cümleleri:**
  - en yüksek pozitif `changeRate` → "X etiketinde %Y artış"
  - `points` içindeki en derin negatif `net` değerli `PROJECTED` nokta → "Z ayında net eksiye
    düşüyor"
  - en uzun `monthsSpan` → "En uzun süredir tekrar eden: W"
- **"Diğer" dilimi** = `total − Σ items.amount` (yalnız `DISTRIBUTED` modda doğru).
- **Kalemin payı** = `amount / periodTotal`.
- **Boş durumlar:** `busiestDay`/`topTag` null, `items: []`, tüm toplamlar 0 — her biri için ayrı
  boş-durum bileşeni; paneli gizleme.

---

## 7. Yapılacaklar

1. `endpoints.json`'ı bu 5 uçla güncelle (parametreler ve response şemaları yukarıdaki
   sözleşmeyle birebir).
2. `src/types/report.ts` içindeki `ReportDTOs` namespace'ini §2'deki haliyle oluştur,
   `src/types/index.ts`'e export ekle.
3. `docs/pages/Reports.md` §8'deki 11 adımlık scaffold sırasını uygula.
4. Ay seçici (year/month) tek bir state'te tutulsun; `summary`, `timeline`, `tag-breakdown`,
   `top-expenses` bu state'e bağlı, `recurring` bağımsız (parametre almıyor).
5. `reports.*` i18n anahtarlarını `tr.json` ve `en.json`'a aynı yapıyla ekle.

---

## 8. Tuzaklar

- `changeRate`, `incomeChangeRate`, `expenseChangeRate` **null olabilir** — `?? 0` yapma, `"—"` göster.
- `deltaVsNext` payda olarak **`current`** kullanır, `next` değil. İki delta da ileri yönde okunur.
- `topTag.tagId` null olabilir (UNTAGGED kazanabilir).
- `label` bir i18n anahtarıdır, ham gösterme.
- `percentage` toplamı `DOUBLE_COUNT` modunda %100'ü aşar — pasta grafikte kullanma.
- `total` / `untaggedAmount` / `monthlyFixedCost` limit'ten bağımsızdır, `items` üzerinden yeniden
  hesaplama.
- `projectedMonthEndExpense` ay başında çok yüksek çıkar; "tahmin" etiketi olmadan gösterme.
- Grafikte `projectedRemainingDebt` çizilir; `cumulativeRemainingDebt` gelecekte düz gider ve
  yanlış okunur.
- `timeline` hem geçmiş hem gelecek içerir; `PROJECTED` noktaları gerçekleşmiş gibi gösterme.
- `amount` alanları taksit bazlıdır: "12.000 TL'lik buzdolabı" değil, "buzdolabının bu aya düşen
  12.000 TL'lik taksiti".
- `store.ts` entegrasyonunun 4 noktasından biri atlanırsa hook'lar sessizce boş döner.

## PROMPT SONU
