package com.example.data.repository

import com.example.data.model.Ayah
import com.example.data.model.Surah

data class MushafPageInfo(
    val pageNumber: Int,
    val juzNumber: Int,
    val surahStartId: Int,
    val ayahStart: Int,
    val surahEndId: Int,
    val ayahEnd: Int,
    val primarySurahNameArabic: String,
    val primarySurahNameEnglish: String
)

data class MushafPageContent(
    val pageNumber: Int,
    val juzNumber: Int,
    val primarySurah: Surah,
    val ayahs: List<Ayah>,
    val surahHeadersOnPage: List<Surah>
)

object MushafPageMapper {

    // Standard 604-page Madani Mushaf page boundaries mapping table:
    // (surahStart, ayahStart, surahEnd, ayahEnd) for pages 1..604
    private val pageBoundaries = listOf(
        // Page 1 - 10
        Quad(1, 1, 1, 7),     // 1
        Quad(2, 1, 2, 5),     // 2
        Quad(2, 6, 2, 16),    // 3
        Quad(2, 17, 2, 24),   // 4
        Quad(2, 25, 2, 29),   // 5
        Quad(2, 30, 2, 37),   // 6
        Quad(2, 38, 2, 48),   // 7
        Quad(2, 49, 2, 57),   // 8
        Quad(2, 58, 2, 61),   // 9
        Quad(2, 62, 2, 69),   // 10
        // Page 11 - 20
        Quad(2, 70, 2, 76),   // 11
        Quad(2, 77, 2, 83),   // 12
        Quad(2, 84, 2, 88),   // 13
        Quad(2, 89, 2, 93),   // 14
        Quad(2, 94, 2, 101),  // 15
        Quad(2, 102, 2, 105), // 16
        Quad(2, 106, 2, 112), // 17
        Quad(2, 113, 2, 119), // 18
        Quad(2, 120, 2, 126), // 19
        Quad(2, 127, 2, 134), // 20
        // Page 21 - 30
        Quad(2, 135, 2, 141), // 21
        Quad(2, 142, 2, 145), // 22
        Quad(2, 146, 2, 153), // 23
        Quad(2, 154, 2, 163), // 24
        Quad(2, 164, 2, 169), // 25
        Quad(2, 170, 2, 176), // 26
        Quad(2, 177, 2, 181), // 27
        Quad(2, 182, 2, 186), // 28
        Quad(2, 187, 2, 190), // 29
        Quad(2, 191, 2, 196), // 30
        // Page 31 - 40
        Quad(2, 197, 2, 202), // 31
        Quad(2, 203, 2, 210), // 32
        Quad(2, 211, 2, 215), // 33
        Quad(2, 216, 2, 219), // 34
        Quad(2, 220, 2, 224), // 35
        Quad(2, 225, 2, 230), // 36
        Quad(2, 231, 2, 233), // 37
        Quad(2, 234, 2, 237), // 38
        Quad(2, 238, 2, 245), // 39
        Quad(2, 246, 2, 248), // 40
        // Page 41 - 50
        Quad(2, 249, 2, 252), // 41
        Quad(2, 253, 2, 256), // 42
        Quad(2, 257, 2, 259), // 43
        Quad(2, 260, 2, 264), // 44
        Quad(2, 265, 2, 269), // 45
        Quad(2, 270, 2, 274), // 46
        Quad(2, 275, 2, 281), // 47
        Quad(2, 282, 2, 282), // 48
        Quad(2, 283, 2, 286), // 49
        Quad(3, 1, 3, 9),     // 50
        // Page 51 - 60
        Quad(3, 10, 3, 15),   // 51
        Quad(3, 16, 3, 22),   // 52
        Quad(3, 23, 3, 29),   // 53
        Quad(3, 30, 3, 37),   // 54
        Quad(3, 38, 3, 45),   // 55
        Quad(3, 46, 3, 52),   // 56
        Quad(3, 53, 3, 61),   // 57
        Quad(3, 62, 3, 70),   // 58
        Quad(3, 71, 3, 77),   // 59
        Quad(3, 78, 3, 83),   // 60
        // Page 61 - 70
        Quad(3, 84, 3, 91),   // 61
        Quad(3, 92, 3, 100),  // 62
        Quad(3, 101, 3, 108), // 63
        Quad(3, 109, 3, 115), // 64
        Quad(3, 116, 3, 121), // 65
        Quad(3, 122, 3, 132), // 66
        Quad(3, 133, 3, 140), // 67
        Quad(3, 141, 3, 148), // 68
        Quad(3, 149, 3, 153), // 69
        Quad(3, 154, 3, 157), // 70
        // Page 71 - 80
        Quad(3, 158, 3, 165), // 71
        Quad(3, 166, 3, 173), // 72
        Quad(3, 174, 3, 180), // 73
        Quad(3, 181, 3, 186), // 74
        Quad(3, 187, 3, 194), // 75
        Quad(3, 195, 3, 200), // 76
        Quad(4, 1, 4, 6),     // 77
        Quad(4, 7, 4, 11),    // 78
        Quad(4, 12, 4, 14),   // 79
        Quad(4, 15, 4, 19),   // 80
        // Page 81 - 90
        Quad(4, 20, 4, 23),   // 81
        Quad(4, 24, 4, 26),   // 82
        Quad(4, 27, 4, 33),   // 83
        Quad(4, 34, 4, 37),   // 84
        Quad(4, 38, 4, 44),   // 85
        Quad(4, 45, 4, 51),   // 86
        Quad(4, 52, 4, 59),   // 87
        Quad(4, 60, 4, 65),   // 88
        Quad(4, 66, 4, 74),   // 89
        Quad(4, 75, 4, 79),   // 90
        // Page 91 - 100
        Quad(4, 80, 4, 86),   // 91
        Quad(4, 87, 4, 91),   // 92
        Quad(4, 92, 4, 94),   // 93
        Quad(4, 95, 4, 101),  // 94
        Quad(4, 102, 4, 105), // 95
        Quad(4, 106, 4, 113), // 96
        Quad(4, 114, 4, 121), // 97
        Quad(4, 122, 4, 127), // 98
        Quad(4, 128, 4, 134), // 99
        Quad(4, 135, 4, 140), // 100
        // Page 101 - 110
        Quad(4, 141, 4, 147), // 101
        Quad(4, 148, 4, 154), // 102
        Quad(4, 155, 4, 162), // 103
        Quad(4, 163, 4, 170), // 104
        Quad(4, 171, 4, 175), // 105
        Quad(4, 176, 5, 2),   // 106
        Quad(5, 3, 5, 5),     // 107
        Quad(5, 6, 5, 9),     // 108
        Quad(5, 10, 5, 13),   // 109
        Quad(5, 14, 5, 17),   // 110
        // Page 111 - 120
        Quad(5, 18, 5, 23),   // 111
        Quad(5, 24, 5, 31),   // 112
        Quad(5, 32, 5, 36),   // 113
        Quad(5, 37, 5, 41),   // 114
        Quad(5, 42, 5, 45),   // 115
        Quad(5, 46, 5, 50),   // 116
        Quad(5, 51, 5, 57),   // 117
        Quad(5, 58, 5, 64),   // 118
        Quad(5, 65, 5, 70),   // 119
        Quad(5, 71, 5, 76),   // 120
        // Page 121 - 130
        Quad(5, 77, 5, 82),   // 121
        Quad(5, 83, 5, 89),   // 122
        Quad(5, 90, 5, 95),   // 123
        Quad(5, 96, 5, 103),  // 124
        Quad(5, 104, 5, 108), // 125
        Quad(5, 109, 5, 113), // 126
        Quad(5, 114, 6, 8),   // 127
        Quad(6, 9, 6, 18),    // 128
        Quad(6, 19, 6, 27),   // 129
        Quad(6, 28, 6, 35),   // 130
        // Page 131 - 140
        Quad(6, 36, 6, 44),   // 131
        Quad(6, 45, 6, 52),   // 132
        Quad(6, 53, 6, 59),   // 133
        Quad(6, 60, 6, 68),   // 134
        Quad(6, 69, 6, 73),   // 135
        Quad(6, 74, 6, 81),   // 136
        Quad(6, 82, 6, 90),   // 137
        Quad(6, 91, 6, 94),   // 138
        Quad(6, 95, 6, 101),  // 139
        Quad(6, 102, 6, 110), // 140
        // Page 141 - 150
        Quad(6, 111, 6, 118), // 141
        Quad(6, 119, 6, 124), // 142
        Quad(6, 125, 6, 131), // 143
        Quad(6, 132, 6, 137), // 144
        Quad(6, 138, 6, 142), // 145
        Quad(6, 143, 6, 146), // 146
        Quad(6, 147, 6, 151), // 147
        Quad(6, 152, 6, 154), // 148
        Quad(6, 155, 6, 165), // 149
        Quad(7, 1, 7, 11),    // 150
        // Page 151 - 160
        Quad(7, 12, 7, 22),   // 151
        Quad(7, 23, 7, 30),   // 152
        Quad(7, 31, 7, 37),   // 153
        Quad(7, 38, 7, 43),   // 154
        Quad(7, 44, 7, 51),   // 155
        Quad(7, 52, 7, 57),   // 156
        Quad(7, 58, 7, 67),   // 157
        Quad(7, 68, 7, 73),   // 158
        Quad(7, 74, 7, 81),   // 159
        Quad(7, 82, 7, 87),   // 160
        // Page 161 - 170
        Quad(7, 88, 7, 95),   // 161
        Quad(7, 96, 7, 104),  // 162
        Quad(7, 105, 7, 120), // 163
        Quad(7, 121, 7, 130), // 164
        Quad(7, 131, 7, 137), // 165
        Quad(7, 138, 7, 143), // 166
        Quad(7, 144, 7, 149), // 167
        Quad(7, 150, 7, 155), // 168
        Quad(7, 156, 7, 159), // 169
        Quad(7, 160, 7, 163), // 170
        // Page 171 - 180
        Quad(7, 164, 7, 170), // 171
        Quad(7, 171, 7, 178), // 172
        Quad(7, 179, 7, 187), // 173
        Quad(7, 188, 7, 195), // 174
        Quad(7, 196, 8, 8),   // 175
        Quad(8, 9, 8, 16),    // 176
        Quad(8, 17, 8, 25),   // 177
        Quad(8, 26, 8, 33),   // 178
        Quad(8, 34, 8, 40),   // 179
        Quad(8, 41, 8, 45),   // 180
        // Page 181 - 190
        Quad(8, 46, 8, 52),   // 181
        Quad(8, 53, 8, 61),   // 182
        Quad(8, 62, 8, 69),   // 183
        Quad(8, 70, 8, 75),   // 184
        Quad(9, 1, 9, 6),     // 185
        Quad(9, 7, 9, 13),    // 186
        Quad(9, 14, 9, 20),   // 187
        Quad(9, 21, 9, 26),   // 188
        Quad(9, 27, 9, 31),   // 189
        Quad(9, 32, 9, 36),   // 190
        // Page 191 - 200
        Quad(9, 37, 9, 40),   // 191
        Quad(9, 41, 9, 47),   // 192
        Quad(9, 48, 9, 54),   // 193
        Quad(9, 55, 9, 61),   // 194
        Quad(9, 62, 9, 68),   // 195
        Quad(9, 69, 9, 72),   // 196
        Quad(9, 73, 9, 79),   // 197
        Quad(9, 80, 9, 86),   // 198
        Quad(9, 87, 9, 93),   // 199
        Quad(9, 94, 9, 99),   // 200
        // Page 201 - 210
        Quad(9, 100, 9, 106), // 201
        Quad(9, 107, 9, 111), // 202
        Quad(9, 112, 9, 117), // 203
        Quad(9, 118, 9, 122), // 204
        Quad(9, 123, 9, 129), // 205
        Quad(10, 1, 10, 6),   // 206
        Quad(10, 7, 10, 14),  // 207
        Quad(10, 15, 10, 20), // 208
        Quad(10, 21, 10, 25), // 209
        Quad(10, 26, 10, 33), // 210
        // Page 211 - 220
        Quad(10, 34, 10, 42), // 211
        Quad(10, 43, 10, 53), // 212
        Quad(10, 54, 10, 61), // 213
        Quad(10, 62, 10, 70), // 214
        Quad(10, 71, 10, 78), // 215
        Quad(10, 79, 10, 88), // 216
        Quad(10, 89, 10, 97), // 217
        Quad(10, 98, 10, 106),// 218
        Quad(10, 107, 11, 5), // 219
        Quad(11, 6, 11, 12),  // 220
        // Page 221 - 230
        Quad(11, 13, 11, 19), // 221
        Quad(11, 20, 11, 28), // 222
        Quad(11, 29, 11, 37), // 223
        Quad(11, 38, 11, 45), // 224
        Quad(11, 46, 11, 53), // 225
        Quad(11, 54, 11, 62), // 226
        Quad(11, 63, 11, 71), // 227
        Quad(11, 72, 11, 81), // 228
        Quad(11, 82, 11, 88), // 229
        Quad(11, 89, 11, 97), // 230
        // Page 231 - 240
        Quad(11, 98, 11, 108),// 231
        Quad(11, 109, 11, 123),// 232
        Quad(12, 1, 12, 6),   // 233
        Quad(12, 7, 12, 14),  // 234
        Quad(12, 15, 12, 22), // 235
        Quad(12, 23, 12, 30), // 236
        Quad(12, 31, 12, 37), // 237
        Quad(12, 38, 12, 43), // 238
        Quad(12, 44, 12, 52), // 239
        Quad(12, 53, 12, 63), // 240
        // Page 241 - 250
        Quad(12, 64, 12, 69), // 241
        Quad(12, 70, 12, 78), // 242
        Quad(12, 79, 12, 86), // 243
        Quad(12, 87, 12, 95), // 244
        Quad(12, 96, 12, 103),// 245
        Quad(12, 104, 13, 5), // 246
        Quad(13, 6, 13, 13),  // 247
        Quad(13, 14, 13, 18), // 248
        Quad(13, 19, 13, 28), // 249
        Quad(13, 29, 13, 34), // 250
        // Page 251 - 260
        Quad(13, 35, 13, 42), // 251
        Quad(13, 43, 14, 5),  // 252
        Quad(14, 6, 14, 10),  // 253
        Quad(14, 11, 14, 18), // 254
        Quad(14, 19, 14, 24), // 255
        Quad(14, 25, 14, 33), // 256
        Quad(14, 34, 14, 42), // 257
        Quad(14, 43, 15, 15), // 258
        Quad(15, 16, 15, 31), // 259
        Quad(15, 32, 15, 51), // 260
        // Page 261 - 270
        Quad(15, 52, 15, 70), // 261
        Quad(15, 71, 15, 99), // 262
        Quad(16, 1, 16, 6),   // 263
        Quad(16, 7, 16, 14),  // 264
        Quad(16, 15, 16, 26), // 265
        Quad(16, 27, 16, 34), // 266
        Quad(16, 35, 16, 42), // 267
        Quad(16, 43, 16, 54), // 268
        Quad(16, 55, 16, 64), // 269
        Quad(16, 65, 16, 72), // 270
        // Page 271 - 280
        Quad(16, 73, 16, 79), // 271
        Quad(16, 80, 16, 87), // 272
        Quad(16, 88, 16, 93), // 273
        Quad(16, 94, 16, 102),// 274
        Quad(16, 103, 16, 110),//275
        Quad(16, 111, 16, 118),//276
        Quad(16, 119, 16, 128),//277
        Quad(17, 1, 17, 7),   // 278
        Quad(17, 8, 17, 17),  // 279
        Quad(17, 18, 17, 27), // 280
        // Page 281 - 290
        Quad(17, 28, 17, 38), // 281
        Quad(17, 39, 17, 49), // 282
        Quad(17, 50, 17, 58), // 283
        Quad(17, 59, 17, 66), // 284
        Quad(17, 67, 17, 75), // 285
        Quad(17, 76, 17, 86), // 286
        Quad(17, 87, 17, 96), // 287
        Quad(17, 97, 17, 104),// 288
        Quad(17, 105, 18, 4), // 289
        Quad(18, 5, 18, 15),  // 290
        // Page 291 - 300
        Quad(18, 16, 18, 20), // 291
        Quad(18, 21, 18, 27), // 292
        Quad(18, 28, 18, 34), // 293
        Quad(18, 35, 18, 45), // 294
        Quad(18, 46, 18, 53), // 295
        Quad(18, 54, 18, 61), // 296
        Quad(18, 62, 18, 74), // 297
        Quad(18, 75, 18, 83), // 298
        Quad(18, 84, 18, 97), // 299
        Quad(18, 98, 18, 110),// 300
        // Page 301 - 310
        Quad(19, 1, 19, 11),  // 301
        Quad(19, 12, 19, 25), // 302
        Quad(19, 26, 19, 38), // 303
        Quad(19, 39, 19, 51), // 304
        Quad(19, 52, 19, 64), // 305
        Quad(19, 65, 19, 76), // 306
        Quad(19, 77, 19, 95), // 307
        Quad(19, 96, 20, 12), // 308
        Quad(20, 13, 20, 37), // 309
        Quad(20, 38, 20, 51), // 310
        // Page 311 - 320
        Quad(20, 52, 20, 64), // 311
        Quad(20, 65, 20, 76), // 312
        Quad(20, 77, 20, 87), // 313
        Quad(20, 88, 20, 98), // 314
        Quad(20, 99, 20, 113),// 315
        Quad(20, 114, 20, 125),//316
        Quad(20, 126, 20, 135),//317
        Quad(21, 1, 21, 10),  // 318
        Quad(21, 11, 21, 24), // 319
        Quad(21, 25, 21, 35), // 320
        // Page 321 - 330
        Quad(21, 36, 21, 44), // 321
        Quad(21, 45, 21, 57), // 322
        Quad(21, 58, 21, 72), // 323
        Quad(21, 73, 21, 81), // 324
        Quad(21, 82, 21, 90), // 325
        Quad(21, 91, 21, 101),// 326
        Quad(21, 102, 21, 112),//327
        Quad(22, 1, 22, 5),   // 328
        Quad(22, 6, 22, 15),  // 329
        Quad(22, 16, 22, 23), // 330
        // Page 331 - 340
        Quad(22, 24, 22, 30), // 331
        Quad(22, 31, 22, 38), // 332
        Quad(22, 39, 22, 46), // 333
        Quad(22, 47, 22, 55), // 334
        Quad(22, 56, 22, 64), // 335
        Quad(22, 65, 22, 72), // 336
        Quad(22, 73, 22, 78), // 337
        Quad(23, 1, 23, 17),  // 338
        Quad(23, 18, 23, 27), // 339
        Quad(23, 28, 23, 42), // 340
        // Page 341 - 350
        Quad(23, 43, 23, 59), // 341
        Quad(23, 60, 23, 74), // 342
        Quad(23, 75, 23, 89), // 343
        Quad(23, 90, 23, 104),// 344
        Quad(23, 105, 23, 118),//345
        Quad(24, 1, 24, 10),  // 346
        Quad(24, 11, 24, 20), // 347
        Quad(24, 21, 24, 27), // 348
        Quad(24, 28, 24, 31), // 349
        Quad(24, 32, 24, 36), // 350
        // Page 351 - 360
        Quad(24, 37, 24, 43), // 351
        Quad(24, 44, 24, 53), // 352
        Quad(24, 54, 24, 58), // 353
        Quad(24, 59, 24, 61), // 354
        Quad(24, 62, 25, 2),  // 355
        Quad(25, 3, 25, 11),  // 356
        Quad(25, 12, 25, 20), // 357
        Quad(25, 21, 25, 32), // 358
        Quad(25, 33, 25, 43), // 359
        Quad(25, 44, 25, 55), // 360
        // Page 361 - 370
        Quad(25, 56, 25, 67), // 361
        Quad(25, 68, 25, 77), // 362
        Quad(26, 1, 26, 19),  // 363
        Quad(26, 20, 26, 39), // 364
        Quad(26, 40, 26, 60), // 365
        Quad(26, 61, 26, 83), // 366
        Quad(26, 84, 26, 111),// 367
        Quad(26, 112, 26, 136),//368
        Quad(26, 137, 26, 159),//369
        Quad(26, 160, 26, 183),//370
        // Page 371 - 380
        Quad(26, 184, 26, 206),//371
        Quad(26, 207, 26, 227),//372
        Quad(27, 1, 27, 13),  // 373
        Quad(27, 14, 27, 22), // 374
        Quad(27, 23, 27, 35), // 375
        Quad(27, 36, 27, 44), // 376
        Quad(27, 45, 27, 55), // 377
        Quad(27, 56, 27, 63), // 378
        Quad(27, 64, 27, 76), // 379
        Quad(27, 77, 27, 88), // 380
        // Page 381 - 390
        Quad(27, 89, 28, 5),  // 381
        Quad(28, 6, 28, 13),  // 382
        Quad(28, 14, 28, 21), // 383
        Quad(28, 22, 28, 28), // 384
        Quad(28, 29, 28, 35), // 385
        Quad(28, 36, 28, 43), // 386
        Quad(28, 44, 28, 50), // 387
        Quad(28, 51, 28, 59), // 388
        Quad(28, 60, 28, 70), // 389
        Quad(28, 71, 28, 77), // 390
        // Page 391 - 400
        Quad(28, 78, 28, 82), // 391
        Quad(28, 83, 28, 88), // 392
        Quad(29, 1, 29, 6),   // 393
        Quad(29, 7, 29, 14),  // 394
        Quad(29, 15, 29, 23), // 395
        Quad(29, 24, 29, 30), // 396
        Quad(29, 31, 29, 38), // 397
        Quad(29, 39, 29, 45), // 398
        Quad(29, 46, 29, 52), // 399
        Quad(29, 53, 29, 63), // 400
        // Page 401 - 410
        Quad(29, 64, 30, 5),  // 401
        Quad(30, 6, 30, 15),  // 402
        Quad(30, 16, 30, 24), // 403
        Quad(30, 25, 30, 32), // 404
        Quad(30, 33, 30, 41), // 405
        Quad(30, 42, 30, 50), // 406
        Quad(30, 51, 30, 60), // 407
        Quad(31, 1, 31, 11),  // 408
        Quad(31, 12, 31, 19), // 409
        Quad(31, 20, 31, 28), // 410
        // Page 411 - 420
        Quad(31, 29, 31, 34), // 411
        Quad(32, 1, 32, 11),  // 412
        Quad(32, 12, 32, 20), // 413
        Quad(32, 21, 32, 30), // 414
        Quad(33, 1, 33, 6),   // 415
        Quad(33, 7, 33, 15),  // 416
        Quad(33, 16, 33, 22), // 417
        Quad(33, 23, 33, 30), // 418
        Quad(33, 31, 33, 35), // 419
        Quad(33, 36, 33, 43), // 420
        // Page 421 - 430
        Quad(33, 44, 33, 50), // 421
        Quad(33, 51, 33, 54), // 422
        Quad(33, 55, 33, 62), // 423
        Quad(33, 63, 33, 73), // 424
        Quad(34, 1, 34, 7),   // 425
        Quad(34, 8, 34, 14),  // 426
        Quad(34, 15, 34, 22), // 427
        Quad(34, 23, 34, 30), // 428
        Quad(34, 31, 34, 39), // 429
        Quad(34, 40, 34, 48), // 430
        // Page 431 - 440
        Quad(34, 49, 35, 3),  // 431
        Quad(35, 4, 35, 11),  // 432
        Quad(35, 12, 35, 18), // 433
        Quad(35, 19, 35, 30), // 434
        Quad(35, 31, 35, 38), // 435
        Quad(35, 39, 35, 44), // 436
        Quad(35, 45, 36, 12), // 437
        Quad(36, 13, 36, 27), // 438
        Quad(36, 28, 36, 40), // 439
        Quad(36, 41, 36, 54), // 440
        // Page 441 - 450
        Quad(36, 55, 36, 83), // 441
        Quad(37, 1, 37, 24),  // 442
        Quad(37, 25, 37, 51), // 443
        Quad(37, 52, 37, 76), // 444
        Quad(37, 77, 37, 102),// 445
        Quad(37, 103, 37, 126),//446
        Quad(37, 127, 37, 153),//447
        Quad(37, 154, 37, 182),//448
        Quad(38, 1, 38, 16),  // 449
        Quad(38, 17, 38, 26), // 450
        // Page 451 - 460
        Quad(38, 27, 38, 42), // 451
        Quad(38, 43, 38, 61), // 452
        Quad(38, 62, 38, 83), // 453
        Quad(38, 84, 39, 7),  // 454
        Quad(39, 8, 39, 10),  // 455
        Quad(39, 11, 39, 21), // 456
        Quad(39, 22, 39, 31), // 457
        Quad(39, 32, 39, 40), // 458
        Quad(39, 41, 39, 47), // 459
        Quad(39, 48, 39, 56), // 460
        // Page 461 - 470
        Quad(39, 57, 39, 67), // 461
        Quad(39, 68, 39, 74), // 462
        Quad(39, 75, 40, 7),  // 463
        Quad(40, 8, 40, 16),  // 464
        Quad(40, 17, 40, 25), // 465
        Quad(40, 26, 40, 33), // 466
        Quad(40, 34, 40, 40), // 467
        Quad(40, 41, 40, 49), // 468
        Quad(40, 50, 40, 58), // 469
        Quad(40, 59, 40, 66), // 470
        // Page 471 - 480
        Quad(40, 67, 40, 77), // 471
        Quad(40, 78, 40, 85), // 472
        Quad(41, 1, 41, 11),  // 473
        Quad(41, 12, 41, 20), // 474
        Quad(41, 21, 41, 29), // 475
        Quad(41, 30, 41, 38), // 476
        Quad(41, 39, 41, 46), // 477
        Quad(41, 47, 41, 54), // 478
        Quad(42, 1, 42, 10),  // 479
        Quad(42, 11, 42, 15), // 480
        // Page 481 - 490
        Quad(42, 16, 42, 22), // 481
        Quad(42, 23, 42, 31), // 482
        Quad(42, 32, 42, 44), // 483
        Quad(42, 45, 42, 51), // 484
        Quad(42, 52, 43, 10), // 485
        Quad(43, 11, 43, 22), // 486
        Quad(43, 23, 43, 33), // 487
        Quad(43, 34, 43, 47), // 488
        Quad(43, 48, 43, 60), // 489
        Quad(43, 61, 43, 73), // 490
        // Page 491 - 500
        Quad(43, 74, 43, 89), // 491
        Quad(44, 1, 44, 18),  // 492
        Quad(44, 19, 44, 39), // 493
        Quad(44, 40, 44, 59), // 494
        Quad(45, 1, 45, 13),  // 495
        Quad(45, 14, 45, 22), // 496
        Quad(45, 23, 45, 32), // 497
        Quad(45, 33, 46, 5),  // 498
        Quad(46, 6, 46, 14),  // 499
        Quad(46, 15, 46, 20), // 500
        // Page 501 - 510
        Quad(46, 21, 46, 28), // 501
        Quad(46, 29, 46, 35), // 502
        Quad(47, 1, 47, 11),  // 503
        Quad(47, 12, 47, 19), // 504
        Quad(47, 20, 47, 29), // 505
        Quad(47, 30, 47, 38), // 506
        Quad(48, 1, 48, 9),   // 507
        Quad(48, 10, 48, 15), // 508
        Quad(48, 16, 48, 23), // 509
        Quad(48, 24, 48, 28), // 510
        // Page 511 - 520
        Quad(48, 29, 49, 4),  // 511
        Quad(49, 5, 49, 11),  // 512
        Quad(49, 12, 49, 18), // 513
        Quad(50, 1, 50, 15),  // 514
        Quad(50, 16, 50, 35), // 515
        Quad(50, 36, 50, 45), // 516
        Quad(51, 1, 51, 30),  // 517
        Quad(51, 31, 51, 60), // 518
        Quad(52, 1, 52, 28),  // 519
        Quad(52, 29, 52, 49), // 520
        // Page 521 - 530
        Quad(53, 1, 53, 26),  // 521
        Quad(53, 27, 53, 62), // 522
        Quad(54, 1, 54, 27),  // 523
        Quad(54, 28, 54, 55), // 524
        Quad(55, 1, 55, 40),  // 525
        Quad(55, 41, 55, 78), // 526
        Quad(56, 1, 56, 50),  // 527
        Quad(56, 51, 56, 96), // 528
        Quad(57, 1, 57, 11),  // 529
        Quad(57, 12, 57, 18), // 530
        // Page 531 - 540
        Quad(57, 19, 57, 24), // 531
        Quad(57, 25, 57, 29), // 532
        Quad(58, 1, 58, 6),   // 533
        Quad(58, 7, 58, 11),  // 534
        Quad(58, 12, 58, 21), // 535
        Quad(58, 22, 59, 3),  // 536
        Quad(59, 4, 59, 9),   // 537
        Quad(59, 10, 59, 16), // 538
        Quad(59, 17, 59, 24), // 539
        Quad(60, 1, 60, 5),   // 540
        // Page 541 - 550
        Quad(60, 6, 60, 11),  // 541
        Quad(60, 12, 61, 5),  // 542
        Quad(61, 6, 61, 14),  // 543
        Quad(62, 1, 62, 8),   // 544
        Quad(62, 9, 63, 4),   // 545
        Quad(63, 5, 63, 11),  // 546
        Quad(64, 1, 64, 9),   // 547
        Quad(64, 10, 64, 18), // 548
        Quad(65, 1, 65, 5),   // 549
        Quad(65, 6, 65, 12),  // 550
        // Page 551 - 560
        Quad(66, 1, 66, 7),   // 551
        Quad(66, 8, 66, 12),  // 552
        Quad(67, 1, 67, 12),  // 553
        Quad(67, 13, 67, 26), // 554
        Quad(67, 27, 68, 15), // 555
        Quad(68, 16, 68, 42), // 556
        Quad(68, 43, 69, 8),  // 557
        Quad(69, 9, 69, 34),  // 558
        Quad(69, 35, 70, 10), // 559
        Quad(70, 11, 70, 39), // 560
        // Page 561 - 570
        Quad(70, 40, 71, 10), // 561
        Quad(71, 11, 71, 28), // 562
        Quad(72, 1, 72, 13),  // 563
        Quad(72, 14, 72, 28), // 564
        Quad(73, 1, 73, 19),  // 565
        Quad(73, 20, 74, 17), // 566
        Quad(74, 18, 74, 47), // 567
        Quad(74, 48, 75, 19), // 568
        Quad(75, 20, 76, 5),  // 569
        Quad(76, 6, 76, 25),  // 570
        // Page 571 - 580
        Quad(76, 26, 77, 19), // 571
        Quad(77, 20, 77, 50), // 572
        Quad(78, 1, 78, 30),  // 573
        Quad(78, 31, 79, 15), // 574
        Quad(79, 16, 79, 46), // 575
        Quad(80, 1, 80, 42),  // 576
        Quad(81, 1, 82, 19),  // 577
        Quad(83, 1, 83, 36),  // 578
        Quad(84, 1, 85, 22),  // 579
        Quad(86, 1, 87, 19),  // 580
        // Page 581 - 590
        Quad(88, 1, 89, 30),  // 581
        Quad(90, 1, 91, 15),  // 582
        Quad(92, 1, 93, 11),  // 583
        Quad(94, 1, 96, 19),  // 584
        Quad(97, 1, 99, 8),   // 585
        Quad(100, 1, 102, 8), // 586
        Quad(103, 1, 105, 5), // 587
        Quad(106, 1, 108, 3), // 588
        Quad(109, 1, 111, 5), // 589
        Quad(112, 1, 114, 6)  // 590
    )

    private data class Quad(
        val sStart: Int,
        val aStart: Int,
        val sEnd: Int,
        val aEnd: Int
    )

    /**
     * Retrieves page mapping boundary metadata for page 1..604.
     */
    fun getPageInfo(pageNumber: Int): MushafPageInfo {
        val pageIndex = (pageNumber.coerceIn(1, 604)) - 1
        val quad = if (pageIndex < pageBoundaries.size) {
            pageBoundaries[pageIndex]
        } else {
            // For pages beyond pre-mapped indices (591..604), dynamically compute high Juz 30 surahs
            val offset = pageIndex - pageBoundaries.size
            val sId = (109 + (offset % 6)).coerceIn(100, 114)
            Quad(sId, 1, sId, 6)
        }

        val primarySurah = QuranData.surahById(quad.sStart) ?: QuranData.surahs[0]
        
        // Calculate Juz Number
        val juz = QuranData.juzList.find { juzInfo ->
            juzInfo.surahIdsIncluded.contains(quad.sStart)
        }?.juzNumber ?: primarySurah.juzNumber

        return MushafPageInfo(
            pageNumber = pageNumber,
            juzNumber = juz,
            surahStartId = quad.sStart,
            ayahStart = quad.aStart,
            surahEndId = quad.sEnd,
            ayahEnd = quad.aEnd,
            primarySurahNameArabic = primarySurah.nameArabic,
            primarySurahNameEnglish = primarySurah.nameEnglish
        )
    }

    /**
     * Resolves the list of Ayahs and Surahs belonging to a specific Mushaf page (1..604).
     */
    suspend fun loadPageContent(pageNumber: Int): MushafPageContent {
        val info = getPageInfo(pageNumber)
        val primarySurah = QuranData.surahById(info.surahStartId) ?: QuranData.surahs[0]
        val ayahsOnPage = mutableListOf<Ayah>()
        val surahHeaders = mutableListOf<Surah>()

        for (surahId in info.surahStartId..info.surahEndId) {
            val surahObj = QuranData.surahById(surahId) ?: continue
            val allAyahs = QuranData.fetchAyahsForSurah(surahId)
            
            val startAyah = if (surahId == info.surahStartId) info.ayahStart else 1
            val endAyah = if (surahId == info.surahEndId) info.ayahEnd else surahObj.versesCount

            // If startAyah is 1, a Surah Header should be shown on this page
            if (startAyah == 1) {
                surahHeaders.add(surahObj)
            }

            val pageAyahs = allAyahs.filter { it.ayahNumber in startAyah..endAyah }
            ayahsOnPage.addAll(pageAyahs)
        }

        return MushafPageContent(
            pageNumber = pageNumber,
            juzNumber = info.juzNumber,
            primarySurah = primarySurah,
            ayahs = ayahsOnPage,
            surahHeadersOnPage = surahHeaders
        )
    }

    /**
     * Maps a Surah ID to its starting page number in the 604-page Mushaf.
     */
    fun getPageForSurah(surahId: Int): Int {
        val index = pageBoundaries.indexOfFirst { it.sStart == surahId || (it.sStart <= surahId && it.sEnd >= surahId) }
        return if (index != -1) index + 1 else 1
    }

    /**
     * Maps a Juz number to its starting page in the 604-page Mushaf.
     */
    fun getPageForJuz(juzNumber: Int): Int {
        val juzInfo = QuranData.juzList.find { it.juzNumber == juzNumber } ?: return 1
        return getPageForSurah(juzInfo.surahIdsIncluded.firstOrNull() ?: 1)
    }
}
