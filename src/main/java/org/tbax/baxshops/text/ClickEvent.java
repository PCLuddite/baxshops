/*
 * Copyright (C) Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.tbax.baxshops.text;

import com.google.gson.JsonObject;

public final class ClickEvent
{
    private String event;
    private String value;

    private ClickEvent(String event, String value)
    {
        this.event = event;
        this.value = value;
    }

    public String getEvent()
    {
        return event;
    }

    public String getValue()
    {
        return value;
    }

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();
        object.addProperty("action", event);
        object.addProperty("value", value);
        return object;
    }

    @Override
    public String toString()
    {
        return toJsonObject().toString();
    }

    public static ClickEvent runCommand(String command)
    {
        return new ClickEvent("run_command", command);
    }

    public static ClickEvent suggestCommand(String command)
    {
        return new ClickEvent("suggest_command", command);
    }

    public static ClickEvent openUrl(String url)
    {
        return new ClickEvent("open_url", url);
    }
}
