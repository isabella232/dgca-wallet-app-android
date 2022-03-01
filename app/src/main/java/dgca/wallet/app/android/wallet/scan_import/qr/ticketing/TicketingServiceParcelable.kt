/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-wallet-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by osarapulov on 10/11/21 7:53 PM
 */

package dgca.wallet.app.android.wallet.scan_import.qr.ticketing

import android.os.Parcelable
import dgca.verifier.app.ticketing.data.identity.TicketingServiceRemote
import kotlinx.parcelize.Parcelize

@Parcelize
data class TicketingServiceParcelable(
    val id: String,
    val type: String,
    val serviceEndpoint: String,
    val name: String
) : Parcelable

fun TicketingServiceRemote.fromRemote(): TicketingServiceParcelable = TicketingServiceParcelable(
    id = id,
    type = type,
    serviceEndpoint = serviceEndpoint,
    name = name
)

fun TicketingServiceParcelable.toRemote(): TicketingServiceRemote = TicketingServiceRemote(
    id = id,
    type = type,
    serviceEndpoint = serviceEndpoint,
    name = name
)